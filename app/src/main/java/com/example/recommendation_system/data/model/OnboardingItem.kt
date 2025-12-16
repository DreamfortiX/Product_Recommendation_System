package com.example.choice.data.model

data class OnboardingItem(
    val title: String,
    val description: String,
    val lottieAnimation: Int,
    val activeCard: Int // 0: Cosine, 1: Decision Tree, 2: GNN
)