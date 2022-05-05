package io.github.fourlastor.monster

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cubemap
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController
import ktx.app.KtxScreen
import net.mgsx.gltf.loaders.glb.GLBLoader
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneAsset
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.scene.SceneSkybox
import net.mgsx.gltf.scene3d.utils.IBLBuilder

class TestScreem : KtxScreen {

    private val sceneAsset: SceneAsset = GLBLoader().load(Gdx.files.internal("../assets/human.glb"))
    private val sceneManager: SceneManager = SceneManager(sceneAsset.maxBones + 1)
    private val scene: Scene = Scene(sceneAsset.scene)
    private val camera: PerspectiveCamera =
        PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
            val d = .02f
            near = d / 1000f
            far = 200f
        }
    private val diffuseCubemap: Cubemap
    private val environmentCubemap: Cubemap
    private val specularCubemap: Cubemap
    private val brdfLUT: Texture
    private val skybox: SceneSkybox
    private val light: DirectionalLightEx = DirectionalLightEx().apply {
        direction.set(1f, -3f, 1f).nor()
        color.set(Color.WHITE)
    }

    private val cameraController = FirstPersonCameraController(camera)

    init {
        sceneManager.addScene(scene)

        Gdx.input.inputProcessor = cameraController
        sceneManager.setCamera(camera)
        sceneManager.environment.add(light)

        // setup quick IBL (image based lighting)
        val iblBuilder = IBLBuilder.createOutdoor(light)
        environmentCubemap = iblBuilder.buildEnvMap(1024)
        diffuseCubemap = iblBuilder.buildIrradianceMap(256)
        specularCubemap = iblBuilder.buildRadianceMap(10)
        iblBuilder.dispose()

        // This texture is provided by the library, no need to have it in your assets.
        brdfLUT = Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"))

        sceneManager.setAmbientLight(1f)
        sceneManager.environment.set(PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT))
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap))
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap))

        // setup skybox
        skybox = SceneSkybox(environmentCubemap)
        sceneManager.setSkyBox(skybox)
    }

    override fun resize(width: Int, height: Int) {
        sceneManager.updateViewport(width.toFloat(), height.toFloat())
    }

    override fun render(delta: Float) {
        cameraController.update()
        sceneManager.update(delta)
        sceneManager.render()
    }

    override fun dispose() {
        sceneManager.dispose()
        sceneAsset.dispose()
        environmentCubemap.dispose()
        diffuseCubemap.dispose()
        specularCubemap.dispose()
        brdfLUT.dispose()
        skybox.dispose()
    }
}