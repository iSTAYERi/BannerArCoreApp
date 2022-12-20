package com.example.arcoretestapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.arcoretestapp.databinding.ActivitySceneBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.rotation
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.rotation
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.ar.node.infos.AugmentedImageInfoNode
import io.github.sceneview.math.Position
import io.github.sceneview.utils.doOnApplyWindowInsets
import io.github.sceneview.utils.setFullScreen

private const val TRACKED_IMAGE_DATABASE_PATH = "banners.imgdb"

class SceneActivity  : AppCompatActivity(R.layout.activity_scene) {

    private val binding: ActivitySceneBinding by viewBinding()

    private data class Model(
        val fileLocation: String,
        val scaleUnits: Float? = null,
        val placementMode: PlacementMode = PlacementMode.DISABLED,
        val applyPoseRotation: Boolean = true
    )

    private val models = listOf(
        Model(fileLocation = "models/logo.glb",
            scaleUnits = 0.5f,
            placementMode = PlacementMode.DISABLED,
            applyPoseRotation = false
        ),
        Model(fileLocation = "models/tel.glb",
            scaleUnits = 0.5f,
            placementMode = PlacementMode.DISABLED,
            applyPoseRotation = false
        ),
        Model(
            fileLocation = "models/dom.glb",
            scaleUnits = 0.5f,
            placementMode = PlacementMode.DISABLED,
            applyPoseRotation = false
        )
    )
    private var modelIndex = 0
    private var modelNode: ArModelNode? = null

    private var isLoading = false
        set(value) {
            field = value
            binding.loadingView.isGone = !value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFullScreen(
            findViewById(R.id.rootView),
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )

        // Configure Augmented Image
        binding.sceneView.configureSession { arSession, config ->
            val imageDatabase = this.assets.open(TRACKED_IMAGE_DATABASE_PATH).use {
                AugmentedImageDatabase.deserialize(arSession, it)
            }
            config.augmentedImageDatabase = imageDatabase
            arSession.configure(config)
        }

        newModelNode()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = !item.isChecked
        modelNode?.detachAnchor()
        modelNode?.placementMode = when (item.itemId) {
            R.id.menuPlanePlacement -> PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL
            R.id.menuInstantPlacement -> PlacementMode.INSTANT
            R.id.menuDepthPlacement -> PlacementMode.DEPTH
            R.id.menuBestPlacement -> PlacementMode.BEST_AVAILABLE
            else -> PlacementMode.DISABLED
        }
        return super.onOptionsItemSelected(item)
    }

    private fun newModelNode() {
        isLoading = true
        modelNode?.takeIf { !it.isAnchored }?.let {
            binding.sceneView.removeChild(it)
            it.destroy()
        }

        val model = models[modelIndex]
        modelIndex = (modelIndex + 1) % models.size
        modelNode = ArModelNode(model.placementMode).apply {
            applyPoseRotation = model.applyPoseRotation
            followHitPosition = false
            loadModelAsync(
                context = this@SceneActivity,
                lifecycle = lifecycle,
                glbFileLocation = model.fileLocation,
                autoAnimate = true,
                scaleToUnits = model.scaleUnits,
                // Place the model origin at the bottom center
                centerOrigin = null
            ) {
                binding.sceneView.planeRenderer.isVisible = true
                isLoading = false
            }
            this.modelRotation = Float3(0.0f, 180.0f, 0.0f)
            onAnchorChanged = { _, _ -> }
            onHitResult = { _, _ -> }
        }

        // Callback for Augmented Image
        binding.sceneView.onAugmentedImageUpdate = mutableListOf(
            { augmentedImage ->
                if (augmentedImage.trackingState == TrackingState.TRACKING) {
                    val anchorImage = augmentedImage.createAnchor(augmentedImage.centerPose)
                    modelNode!!.anchor = anchorImage
                    binding.sceneView.addChild(modelNode!!)
                }
            }
        )

//         Select the model node by default (the model node is also selected on tap)
        binding.sceneView.selectedNode = modelNode
    }
}