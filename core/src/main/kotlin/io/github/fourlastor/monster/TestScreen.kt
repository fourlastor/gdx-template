package io.github.fourlastor.monster

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cubemap
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.IntIntMap
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

class TestScreen : KtxScreen {

    private val sceneAsset: SceneAsset = GLBLoader().load(Gdx.files.internal("../assets/character.glb"))
    private val sceneManager: SceneManager = SceneManager(sceneAsset.maxBones + 1)
    private val characterScene: Scene = Scene(sceneAsset.scene).apply {
        animationController.animate("idle", -1, 1f, null, 0f)
    }
    private val camera: PerspectiveCamera =
        PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
            translate(Vector3(2f, 2f, -3f))
            lookAt(Vector3.Zero)
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

    private val cameraController = object : InputAdapter() {
        private val keys = IntIntMap()

        override fun keyDown(keycode: Int): Boolean {
            keys.put(keycode, keycode)
            return true
        }

        override fun keyUp(keycode: Int): Boolean {
            keys.remove(keycode, 0)
            return true
        }

        fun update(delta: Float) {
            val rotation = delta * 100
            when {
                keys.containsKey(Input.Keys.A) -> {
                    camera.rotateAround(Vector3.Y, Vector3.Y, rotation)
                    camera.lookAt(Vector3.Zero)
                    camera.update(true)
                }
                keys.containsKey(Input.Keys.D) -> {
                    camera.rotateAround(Vector3.Y, Vector3.Y, -rotation)
                    camera.lookAt(Vector3.Zero)
                    camera.update(true)
                }
            }
        }
    }

    init {
        sceneManager.addScene(characterScene)

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

    override fun show() {
        Gdx.input.inputProcessor = cameraController
    }

    override fun resize(width: Int, height: Int) {
        sceneManager.updateViewport(width.toFloat(), height.toFloat())
    }

    override fun render(delta: Float) {
        cameraController.update(delta)
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