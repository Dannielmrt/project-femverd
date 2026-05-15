package com.example.femverd.ui.screens.certificate

import android.content.ContentValues
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.femverd.data.TokenManager
import com.example.femverd.model.CertificateResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateScreen(
    navController: NavController,
    viewModel: CertificateViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val isLoading by viewModel.isLoading.collectAsState()
    val certificate by viewModel.certificateData.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        tokenManager.getToken()?.let { viewModel.fetchCertificate(it) }
    }

    Scaffold(

        containerColor = Color(0xFFF5F5F7),
        topBar = {
            TopAppBar(
                title = { Text("Tax Certificate") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F7),
                    titleContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            if (certificate != null) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            generatePdfAndSave(context, certificate!!)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Download PDF")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp), // Margen exterior
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            } else if (certificate != null) {

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Header Section
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Official Seal",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "OFFICIAL RECORD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "Environmental Impact",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        HorizontalDivider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(32.dp))

                        // Citizen Information Section
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            Text("CITIZEN INFORMATION", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Name: ${certificate!!.citizen_name}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                            Text("ID / DNI: ${certificate!!.citizen_dni}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                            Text("Fiscal Year: ${certificate!!.certificate_year}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Recycling Breakdown Section
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            Text("RECYCLING BREAKDOWN", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (certificate!!.materials_breakdown.isEmpty()) {
                                Text("No recycling records found for this year.", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                            } else {
                                certificate!!.materials_breakdown.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.material, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                                        Text("${item.total_quantity} ${item.unit}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        HorizontalDivider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Total Impact Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL POINTS GENERATED", style = MaterialTheme.typography.titleSmall, color = Color.Black)
                            Text(
                                text = String.format("%.1f", certificate!!.total_points_generated),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        Text(
                            text = "This document is digitally signed by the FemVerd system and is valid for local tax reduction purposes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
suspend fun generatePdfAndSave(context: android.content.Context, certificate: CertificateResponse) {
    /*
      Generates a PDF version of the certificate and saves it to the device's Downloads directory.
     */
    withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()

            // Define standard A4 page size (595 width)
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 16f
            }
            val titlePaint = Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 20f
                isFakeBoldText = true
            }

            var yPosition = 100f
            val xMargin = 50f

            canvas.drawText("OFFICIAL ENVIRONMENTAL", xMargin, yPosition, titlePaint)
            yPosition += 30f
            canvas.drawText("IMPACT CERTIFICATE", xMargin, yPosition, titlePaint)
            yPosition += 60f

            canvas.drawText("Fiscal Year: ${certificate.certificate_year}", xMargin, yPosition, paint)
            yPosition += 30f
            canvas.drawText("Citizen: ${certificate.citizen_name}", xMargin, yPosition, paint)
            yPosition += 30f
            canvas.drawText("DNI: ${certificate.citizen_dni}", xMargin, yPosition, paint)
            yPosition += 50f

            canvas.drawText("----- RECYCLING BREAKDOWN -----", xMargin, yPosition, paint)
            yPosition += 30f

            certificate.materials_breakdown.forEach { item ->
                canvas.drawText("${item.material}: ${item.total_quantity} ${item.unit}", xMargin + 20f, yPosition, paint)
                yPosition += 30f
            }

            yPosition += 20f
            canvas.drawText("TOTAL POINTS GENERATED: ${String.format("%.1f", certificate.total_points_generated)}", xMargin, yPosition, paint)

            yPosition += 80f
            paint.textSize = 12f
            paint.color = android.graphics.Color.GRAY

            canvas.drawText("This document is digitally signed by the FemVerd system", xMargin, yPosition, paint)
            yPosition += 18f
            canvas.drawText("and is valid for local tax reduction purposes.", xMargin, yPosition, paint)

            pdfDocument.finishPage(page)

            // Save to the device's "Downloads" folder
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "FemVerd_Certificate_${certificate.certificate_year}.pdf")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri).use { outputStream: OutputStream? ->
                    if (outputStream != null) {
                        pdfDocument.writeTo(outputStream)
                    }
                }
                pdfDocument.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "PDF saved to Downloads!", Toast.LENGTH_LONG).show()
                }
            } else {
                throw Exception("Could not create URI for PDF")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}