package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AuthUiState
import com.example.ui.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: JournalViewModel,
    onLoginSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo Header - Matching the professional layout specs
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2B2930))
                    .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security App Logo",
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to Daily Journal",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6E1E5)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add encrypted reflections, private photos, and memories securely.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFCCC4D0)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Toggle Tabs: Email vs Phone (Matching bottom border style list selection)
            TabRow(
                selectedTabIndex = if (authState.isEmailSelected) 0 else 1,
                containerColor = Color(0xFF2B2930),
                contentColor = Color(0xFFD0BCFF),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (authState.isEmailSelected) 0 else 1]),
                        color = Color(0xFFD0BCFF)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = authState.isEmailSelected,
                    onClick = { viewModel.selectLoginMethod(true) },
                    text = { Text("Email Verification", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("email_tab")
                )
                Tab(
                    selected = !authState.isEmailSelected,
                    onClick = { viewModel.selectLoginMethod(false) },
                    text = { Text("Mobile Phone OTP", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("phone_tab")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact Field Input
            OutlinedTextField(
                value = authState.contactInput,
                onValueChange = { viewModel.onContactInputChanged(it) },
                label = { Text(if (authState.isEmailSelected) "Email Address" else "Mobile Number") },
                placeholder = { Text(if (authState.isEmailSelected) "user@example.com" else "+1 555-0199") },
                leadingIcon = {
                    Icon(
                        imageVector = if (authState.isEmailSelected) Icons.Default.Email else Icons.Default.Phone,
                        contentDescription = if (authState.isEmailSelected) "Email Icon" else "Phone Icon",
                        tint = Color(0xFFCCC4D0)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (authState.isEmailSelected) KeyboardType.Email else KeyboardType.Phone
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFE6E1E5),
                    unfocusedTextColor = Color(0xFFE6E1E5),
                    focusedLabelColor = Color(0xFFD0BCFF),
                    unfocusedLabelColor = Color(0xFFCCC4D0),
                    focusedBorderColor = Color(0xFFD0BCFF),
                    unfocusedBorderColor = Color(0xFF4A4458)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_input"),
                enabled = !authState.otpSent && !authState.isSendingOtp
            )

            // Showing Send OTP Button
            AnimatedVisibility(
                visible = !authState.otpSent,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.sendOtp() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("send_otp_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = authState.contactInput.isNotBlank() && !authState.isSendingOtp
                    ) {
                        if (authState.isSendingOtp) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF381E72))
                        } else {
                            Text("Get Secured OTP Code", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                }
            }

            // OTP Block (If OTP is active/sent)
            AnimatedVisibility(
                visible = authState.otpSent,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "A verification code has to be simulated below:",
                        color = Color(0xFFCCC4D0),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Safe OTP Gateway Card Styled to match the card borders theme
                    authState.simulatedCode?.let { code ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2B2930))
                                .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(12.dp))
                                .clickable { viewModel.onOtpInputChanged(code) }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SIMULATED SECURE GATEWAY OTP",
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = code,
                                    color = Color(0xFFE6E1E5),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 4.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚡ Click to autofill verification code",
                                    color = Color(0xFFCCC4D0).copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = authState.otpInput,
                        onValueChange = { viewModel.onOtpInputChanged(it) },
                        label = { Text("6-Digit OTP Code") },
                        placeholder = { Text("123456") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Verification Code",
                                tint = Color(0xFFCCC4D0)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFFE6E1E5),
                            unfocusedTextColor = Color(0xFFE6E1E5),
                            focusedLabelColor = Color(0xFFD0BCFF),
                            unfocusedLabelColor = Color(0xFFCCC4D0),
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF4A4458)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_input"),
                        enabled = !authState.isVerifying
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.verifyOtp() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF81C784), // Material Green Success Accent
                            contentColor = Color(0xFF1C1B1F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("verify_otp_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = authState.otpInput.length >= 6 && !authState.isVerifying
                    ) {
                        if (authState.isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF1C1B1F))
                        } else {
                            Text("Verify Code & Reflect", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (authState.countdown > 0) "Resend in ${authState.countdown}s" else "Didn't receive code?",
                            color = Color(0xFFCCC4D0),
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (authState.countdown == 0) {
                            Text(
                                text = "Resend Code",
                                color = Color(0xFFD0BCFF),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .clickable { viewModel.sendOtp() }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            // Error Message Banner
            authState.otpError?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFCF6679).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFCF6679)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFCF6679),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
