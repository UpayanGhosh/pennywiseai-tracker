package com.pennywiseai.tracker.data.repository

import android.content.Context
import android.os.Environment
import android.util.Log
import com.pennywiseai.tracker.core.LlmModel
import com.pennywiseai.tracker.core.LlmModelRegistry
import com.pennywiseai.tracker.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val _modelState = MutableStateFlow(ModelState.NOT_DOWNLOADED)
    val modelState: Flow<ModelState> = _modelState.asStateFlow()

    /** Every model the user can pick from the download list. */
    val availableModels: List<LlmModel> = LlmModelRegistry.ALL

    /** The model the user has selected (persisted), or the default when unset. */
    suspend fun getSelectedModel(): LlmModel =
        LlmModelRegistry.fromId(userPreferencesRepository.getSelectedModelId())

    fun getModelFile(model: LlmModel): File {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), model.fileName)
        Log.d("ModelRepository", "Model file path: ${file.absolutePath}")
        return file
    }

    fun isModelDownloaded(model: LlmModel): Boolean {
        val modelFile = getModelFile(model)
        val exists = modelFile.exists()
        val size = if (exists) modelFile.length() else 0
        val expectedSize = model.sizeBytes
        // Allow 5% variance in file size as download sizes can vary
        // But also accept any file over 2GB as models can vary in size
        val minSize = minOf((expectedSize * 0.95).toLong(), 2L * 1024L * 1024L * 1024L) // 95% of expected or 2GB minimum
        val isDownloaded = exists && size >= minSize

        Log.d("ModelRepository", "Checking ${model.id}: exists=$exists, size=$size bytes (${size/1024/1024}MB), expectedSize=$expectedSize, minSize=$minSize, isDownloaded=$isDownloaded")
        return isDownloaded
    }

    fun updateModelState(state: ModelState) {
        Log.d("ModelRepository", "Updating model state from ${_modelState.value} to $state")
        _modelState.value = state
    }

    /** Refreshes [modelState] against whichever model is currently selected. */
    suspend fun checkModelState() {
        val model = getSelectedModel()
        val newState = if (isModelDownloaded(model)) {
            ModelState.READY
        } else {
            ModelState.NOT_DOWNLOADED
        }
        Log.d("ModelRepository", "checkModelState: ${model.id} -> $newState")
        _modelState.value = newState
    }

    /** Persists the chosen model and refreshes state to match it. */
    suspend fun selectModel(id: String) {
        userPreferencesRepository.setSelectedModelId(id)
        checkModelState()
    }

    fun deleteModel(model: LlmModel): Boolean {
        val modelFile = getModelFile(model)
        return if (modelFile.exists()) {
            val deleted = modelFile.delete()
            if (deleted) {
                _modelState.value = ModelState.NOT_DOWNLOADED
            }
            deleted
        } else {
            false
        }
    }
}

enum class ModelState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    READY,
    LOADING,
    ERROR
}
