package com.trueq.app.util

import com.trueq.app.data.remote.API_ORIGIN

// Los PDFs se sirven como archivos estáticos desde el propio backend
// (wwwroot/Legal), igual que las fotos de productos en /Uploads.
const val URL_TERMINOS_Y_CONDICIONES = "$API_ORIGIN/Legal/TrueQ_Terminos_y_Condiciones.pdf"
const val URL_AVISO_DE_PRIVACIDAD = "$API_ORIGIN/Legal/TrueQ_Aviso_de_Privacidad.pdf"
