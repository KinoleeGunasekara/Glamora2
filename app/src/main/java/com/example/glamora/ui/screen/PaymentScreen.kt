package com.example.glamora.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glamora.R
import com.example.glamora.viewmodel.CartViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.launch

// Note: Ensure you have R.string.format_currency_amount defined in your strings.xml,
// for example: <string name="format_currency_amount">$%.2f</string>

// --- Card Types ---
sealed class CardType(val name: String, val drawableResId: Int) {
    object Visa : CardType("VISA", R.drawable.img_visa_logo)
    object Mastercard : CardType("MASTERCARD", R.drawable.img_mastercard_logo)
    object RuPay : CardType("RUPAY", R.drawable.img_rupay_logo)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    cartViewModel: CartViewModel? = null
) {
    // State management for UI elements
    var selectedCardType by remember { mutableStateOf<CardType>(CardType.Mastercard) }
    var cardHolderName by remember { mutableStateOf(TextFieldValue("")) }
    var cardNumberTfv by remember { mutableStateOf(TextFieldValue("")) }
    var expiryDate by remember { mutableStateOf(TextFieldValue("")) }
    var cvv by remember { mutableStateOf(TextFieldValue("")) }
    var saveCardInfo by remember { mutableStateOf(true) }

    // Order completion state management
    val coroutineScope = rememberCoroutineScope()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var orderNumber by remember { mutableStateOf("") }
    var isProcessingPayment by remember { mutableStateOf(false) }

    // Snackbar setup
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- Validation Logic ---
    val isNameValid = cardHolderName.text.isNotBlank()
    // Validation: 16 digits (19 chars including spaces)
    val isCardNumberValid = cardNumberTfv.text.replace(" ", "").length == 16
    // Validation: MM/YYYY format
    val isExpiryValid = expiryDate.text.matches(Regex("^\\d{2}/\\d{4}$"))
    // Validation: 3 digits
    val isCvvValid = cvv.text.length == 3

    val isFormValid = isNameValid && isCardNumberValid && isExpiryValid && isCvvValid


    // Formatting functions (kept for form display consistency)
    val formatCardNumber: (String) -> String = { input ->
        val digits = input.filter { it.isDigit() }
        digits.chunked(4).joinToString(" ").take(19)
    }

    val formatExpiryDate: (String) -> String = { input ->
        val digits = input.filter { it.isDigit() }
        if (digits.length > 2) {
            val month = digits.substring(0, 2).take(2)
            val year = digits.substring(2).take(4)
            "$month/$year"
        } else digits.take(2)
    }

    // Original payment success dialog (can be removed if not needed later)
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.popBackStack()
            },
            title = { Text("Payment Successful!") },
            text = { Text("Order $orderNumber has been placed successfully.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.popBackStack()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Add Snackbar Host
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_add_card_details)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.content_desc_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (isFormValid) {
                        isProcessingPayment = true
                        coroutineScope.launch {
                            // Complete the order and get order number
                            val generatedOrderNumber = cartViewModel?.completeOrder()
                            if (generatedOrderNumber != null) {

                                // Save card details if user opted to save card (checkbox was checked)
                                cartViewModel.saveCardDetails(
                                    cardHolderName = cardHolderName.text,
                                    cardNumber = cardNumberTfv.text,
                                    expiryDate = expiryDate.text,
                                    cardType = selectedCardType.name,
                                    saveCard = saveCardInfo // This is the existing checkbox state
                                )

                                orderNumber = generatedOrderNumber
                                showSuccessDialog = true
                                isProcessingPayment = false
                            } else {
                                isProcessingPayment = false
                                // Show error if order creation failed
                                snackbarHostState.showSnackbar(
                                    message = "Failed to create order. Please try again.",
                                    actionLabel = "OK",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    } else {
                        // Show validation error
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Please fill in all required payment details correctly.",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                enabled = !isProcessingPayment,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .height(56.dp)
            ) {
                if (isProcessingPayment) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        "Complete Payment",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.label_choose_card),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardSelectionChip(CardType.Visa, selectedCardType) { selectedCardType = it }
                CardSelectionChip(CardType.Mastercard, selectedCardType) { selectedCardType = it }
                CardSelectionChip(CardType.RuPay, selectedCardType) { selectedCardType = it }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.label_card_holder_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = cardHolderName,
                onValueChange = { cardHolderName = it },
                isError = cardHolderName.text.isNotEmpty() && !isNameValid,
                label = { Text(stringResource(R.string.hint_name)) },
                trailingIcon = {
                    Icon(Icons.Default.CreditCard, contentDescription = stringResource(R.string.content_desc_card_icon))
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.label_card_number),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = cardNumberTfv,
                onValueChange = { newValue ->
                    val formatted = formatCardNumber(newValue.text)
                    cardNumberTfv = newValue.copy(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )
                },
                isError = cardNumberTfv.text.isNotEmpty() && !isCardNumberValid,
                label = { Text(stringResource(R.string.hint_card_number)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Icon(Icons.Default.CreditCard, contentDescription = stringResource(R.string.content_desc_card_icon))
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.label_expiry_date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { newValue ->
                            val formatted = formatExpiryDate(newValue.text)
                            expiryDate = newValue.copy(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        },
                        isError = expiryDate.text.isNotEmpty() && !isExpiryValid,
                        label = { Text(stringResource(R.string.hint_expiry_date)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.content_desc_calendar))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.label_cvv),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { newValue ->
                            val filtered = newValue.text.filter { it.isDigit() }.take(3)
                            cvv = newValue.copy(text = filtered)
                        },
                        isError = cvv.text.isNotEmpty() && !isCvvValid,
                        label = { Text(stringResource(R.string.hint_cvv)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        trailingIcon = {
                            Icon(Icons.Default.HelpOutline, contentDescription = stringResource(R.string.content_desc_help))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { saveCardInfo = !saveCardInfo }
            ) {
                Checkbox(
                    checked = saveCardInfo,
                    onCheckedChange = { saveCardInfo = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Text(stringResource(R.string.checkbox_save_card), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// --- Card Selection Chip ---
@Composable
fun RowScope.CardSelectionChip(
    type: CardType,
    selectedType: CardType,
    onSelect: (CardType) -> Unit
) {
    val isSelected = type == selectedType
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onSelect(type) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = type.drawableResId),
                contentDescription = type.name,
                modifier = Modifier.height(30.dp)
            )
        }
    }
}

// --- Payment Success Dialog (Kept in case needed later) ---
@Composable
fun PaymentSuccessDialog(
    amount: String,
    transactionId: String,
    onDismiss: () -> Unit
) {
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.button_ok)) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.content_desc_success_check),
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(50))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.title_payment_confirmed),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    DetailRow(stringResource(R.string.label_amount), stringResource(R.string.format_currency_amount, amount))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow(stringResource(R.string.label_date), currentDate)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow(stringResource(R.string.label_transaction_id), transactionId)
                }
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
    }
}
