package com.notnex.myday.neuralnetwork

enum class AiProvider(
    val displayName: String,
    val endpoint: String,
    val model: String,
) {
    GEMINI("Gemini", "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions", "gemini-flash-latest"),
    MISTRAL("Mistral", "https://api.mistral.ai/v1/chat/completions", "mistral-small-latest"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/chat/completions", "deepseek-chat"),
}