package com.example.femverd.ui.screens.certificate

import android.content.ContentValues
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.femverd.R
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
        containerColor = colorResource(id = R.color.certificate_background),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_certificate)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.desc_go_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.certificate_background),
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
                    Icon(
                        Icons.Default.Download,
                        contentDescription = stringResource(id = R.string.desc_download_pdf)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            } else if (certificate != null) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = dimensionResource(
                            id = R.dimen.card_elevation_medium
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = stringResource(id = R.string.desc_official_seal),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_huge))
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                            Column {
                                Text(
                                    text = stringResource(id = R.string.label_official_record),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = stringResource(id = R.string.label_environmental_impact),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))
                        HorizontalDivider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                stringResource(id = R.string.label_citizen_info),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_small)))
                            Text(
                                stringResource(
                                    id = R.string.format_name,
                                    certificate!!.citizen_name
                                ), style = MaterialTheme.typography.bodyLarge, color = Color.Black
                            )
                            Text(
                                stringResource(
                                    id = R.string.format_dni,
                                    certificate!!.citizen_dni
                                ), style = MaterialTheme.typography.bodyLarge, color = Color.Black
                            )
                            Text(
                                stringResource(
                                    id = R.string.format_fiscal_year,
                                    certificate!!.certificate_year.toString()
                                ), style = MaterialTheme.typography.bodyLarge, color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                stringResource(id = R.string.label_recycling_breakdown),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_small)))

                            if (certificate!!.materials_breakdown.isEmpty()) {
                                Text(
                                    stringResource(id = R.string.msg_no_records_year),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                            } else {
                                certificate!!.materials_breakdown.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = dimensionResource(id = R.dimen.spacing_micro)),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            item.material,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Black
                                        )
                                        Text(
                                            stringResource(
                                                id = R.string.format_material_quantity,
                                                item.total_quantity.toString(),
                                                item.unit
                                            ),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))
                        HorizontalDivider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(id = R.string.label_total_points),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.Black
                            )
                            Text(
                                text = String.format("%.1f", certificate!!.total_points_generated),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_huge)))

                        Text(
                            text = stringResource(id = R.string.msg_certificate_validity),
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
    withContext(Dispatchers.IO) {
        /*
          Generates a PDF version of the certificate and saves it to the device's Downloads directory.
         */
        try {
            val pdfDocument = PdfDocument()

            // Standard A4 page size
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            // Designed with canvas library
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

            canvas.drawText(
                context.getString(R.string.pdf_title_l1),
                xMargin,
                yPosition,
                titlePaint
            )
            yPosition += 30f
            canvas.drawText(
                context.getString(R.string.pdf_title_l2),
                xMargin,
                yPosition,
                titlePaint
            )
            yPosition += 60f

            canvas.drawText(
                context.getString(
                    R.string.format_fiscal_year,
                    certificate.certificate_year.toString()
                ), xMargin, yPosition, paint
            )
            yPosition += 30f
            canvas.drawText(
                context.getString(R.string.pdf_citizen, certificate.citizen_name),
                xMargin,
                yPosition,
                paint
            )
            yPosition += 30f
            canvas.drawText(
                context.getString(R.string.pdf_dni, certificate.citizen_dni),
                xMargin,
                yPosition,
                paint
            )
            yPosition += 50f

            canvas.drawText(
                context.getString(R.string.pdf_breakdown_header),
                xMargin,
                yPosition,
                paint
            )
            yPosition += 30f

            certificate.materials_breakdown.forEach { item ->
                val quantityText = context.getString(
                    R.string.format_material_quantity,
                    item.total_quantity.toString(),
                    item.unit
                )
                canvas.drawText("${item.material}: $quantityText", xMargin + 20f, yPosition, paint)
                yPosition += 30f
            }

            yPosition += 20f
            val totalPtsText = String.format("%.1f", certificate.total_points_generated)
            canvas.drawText(
                context.getString(R.string.pdf_total_points, totalPtsText),
                xMargin,
                yPosition,
                paint
            )

            yPosition += 80f
            paint.textSize = 12f
            paint.color = android.graphics.Color.GRAY

            canvas.drawText(context.getString(R.string.pdf_validity_l1), xMargin, yPosition, paint)
            yPosition += 18f
            canvas.drawText(context.getString(R.string.pdf_validity_l2), xMargin, yPosition, paint)

            pdfDocument.finishPage(page)

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    context.getString(
                        R.string.pdf_filename,
                        certificate.certificate_year.toString()
                    )
                )
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
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
                    Toast.makeText(
                        context,
                        context.getString(R.string.msg_pdf_saved),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                throw Exception("Could not create URI for PDF")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.msg_pdf_error, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}