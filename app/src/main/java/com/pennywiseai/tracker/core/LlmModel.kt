package com.pennywiseai.tracker.core

/**
 * A downloadable on-device LLM in LiteRT-LM (`.litertlm`) format.
 *
 * Every selectable model the user can download lives in [LlmModelRegistry]. The
 * app loads the selected model's file into the LiteRT-LM `Engine` (CPU backend),
 * so only generic CPU `.litertlm` builds belong here — not the hardware-specific
 * (`_qualcomm`, `_intel`, Tensor) or `-web` variants.
 *
 * @param id            Stable identifier persisted in preferences. Never change it
 *                      for an existing model or users lose their selection.
 * @param displayName   Shown in the model picker.
 * @param family        Short family label for grouping/badging (e.g. "Qwen", "Gemma").
 * @param description   One-line blurb shown under the name in the picker.
 * @param fileName      On-disk file name (also the download destination name).
 * @param downloadUrl   Direct download URL. Must be reachable without auth headers
 *                      (Android `DownloadManager` is used with a plain GET).
 * @param sizeBytes     Exact file size in bytes. Used for the download-complete
 *                      size check and for the ~3GB free-space pre-check.
 */
data class LlmModel(
    val id: String,
    val displayName: String,
    val family: String,
    val description: String,
    val fileName: String,
    val downloadUrl: String,
    val sizeBytes: Long,
) {
    /** Human-friendly size in whole MB (mirrors the old `MODEL_SIZE_MB`). */
    val sizeMb: Long get() = sizeBytes / (1024L * 1024L)

    /** Free space required before downloading — 2x the model size for safety. */
    val requiredSpaceBytes: Long get() = sizeBytes * 2
}

/**
 * Catalog of the models offered in the download picker.
 *
 * Qwen stays the [DEFAULT] so existing installs (which already have its file on
 * disk) keep working without a re-download. The Gemma 4 entries point at the
 * `litert-community` repos, which are publicly downloadable (not license-gated),
 * so they work with the same plain-URL `DownloadManager` flow as Qwen.
 */
object LlmModelRegistry {

    val QWEN_2_5_1_5B = LlmModel(
        id = "qwen2_5_1_5b_instruct",
        displayName = "Qwen 2.5 1.5B Instruct",
        family = "Qwen",
        description = "Compact and fast. Recommended default.",
        fileName = "Qwen2.5-1.5B-Instruct-q8-ekv4096.litertlm",
        downloadUrl = "https://pub-fcfb3ffddb184540a758a7fe68249908.r2.dev/Qwen2.5-1.5B-Instruct-q8-ekv4096.litertlm",
        sizeBytes = 1_597_931_520L,
    )

    val GEMMA_4_E2B = LlmModel(
        id = "gemma_4_e2b_it",
        displayName = "Gemma 4 E2B",
        family = "Gemma",
        description = "Google's Gemma 4, tuned for mobile. Larger but more capable.",
        fileName = "gemma-4-E2B-it.litertlm",
        downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
        sizeBytes = 2_588_147_712L,
    )

    val GEMMA_4_E4B = LlmModel(
        id = "gemma_4_e4b_it",
        displayName = "Gemma 4 E4B",
        family = "Gemma",
        description = "Largest Gemma 4. Best quality; needs more storage and RAM.",
        fileName = "gemma-4-E4B-it.litertlm",
        downloadUrl = "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm",
        sizeBytes = 3_659_530_240L,
    )

    /** All selectable models, in picker display order. */
    val ALL: List<LlmModel> = listOf(QWEN_2_5_1_5B, GEMMA_4_E2B, GEMMA_4_E4B)

    /** Selected when the user has never chosen a model. Keep this as Qwen. */
    val DEFAULT: LlmModel = QWEN_2_5_1_5B

    /** Resolve a persisted id back to a model, falling back to [DEFAULT]. */
    fun fromId(id: String?): LlmModel = ALL.firstOrNull { it.id == id } ?: DEFAULT
}
