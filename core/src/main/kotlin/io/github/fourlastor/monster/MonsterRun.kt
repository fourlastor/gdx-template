package io.github.fourlastor.monster

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController
import net.mgsx.gltf.loaders.glb.GLBLoader
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneAsset
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.scene.SceneSkybox
import net.mgsx.gltf.scene3d.utils.IBLBuilder


class MonsterRun : ApplicationAdapter() {

    private lateinit var sceneManager: SceneManager
    private lateinit var sceneAsset: SceneAsset
    private lateinit var scene: Scene
    private lateinit var camera: PerspectiveCamera
    private lateinit var diffuseCubemap: Cubemap
    private lateinit var environmentCubemap: Cubemap
    private lateinit var specularCubemap: Cubemap
    private lateinit var brdfLUT: Texture
    private lateinit var skybox: SceneSkybox
    private lateinit var light: DirectionalLightEx
    private lateinit var cameraController: FirstPersonCameraController

    override fun create() {
        val sceneAsset: SceneAsset = GLBLoader().load(Gdx.files.internal("../assets/human.glb"))
        scene = Scene(sceneAsset.scene)
        sceneManager = SceneManager(sceneAsset.maxBones + 1)
        sceneManager.addScene(scene)

        // setup camera (The BoomBox model is very small so you may need to adapt camera settings for your scene)
        camera = PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        cameraController = FirstPersonCameraController(camera)
        Gdx.input.inputProcessor = cameraController
        val d = .02f
        camera.near = d / 1000f
        camera.far = 200f
        sceneManager.setCamera(camera)

        // setup light
        light = DirectionalLightEx()
        light.direction.set(1f, -3f, 1f).nor()
        light.color.set(Color.WHITE)
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

    override fun render() {
        val deltaTime = Gdx.graphics.deltaTime

        cameraController.update()

        // render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        sceneManager.update(deltaTime)
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