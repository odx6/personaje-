package hellojme3;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class HelloJME3 extends SimpleApplication implements ActionListener, MouseWheelListener, MouseListener {

    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private Vector3f vision = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;
    private TerrainQuad terrain;
    private Material mat_terrain;

    public static void main(String[] args) {
        HelloJME3 app = new HelloJME3();
        app.start(); // start the game
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        flyCam.setMoveSpeed(100);
        setUpKeys();

        float[] heightMap = new float[65 * 513];
        terrain = new TerrainQuad("my terrain", 65, 513, heightMap);

        mat_terrain = new Material(assetManager,
                "Common/MatDefs/Terrain/Terrain.j3md");

        Texture grass = assetManager.loadTexture(
                "hellojme3/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex1", grass);
        mat_terrain.setFloat("Tex1Scale", 64f);

        terrain.setMaterial(mat_terrain);
        terrain.setLocalTranslation(0, -10, 0);
        terrain.setLocalScale(9f, 0f, 9f);

        TerrainLodControl lodControl = new TerrainLodControl(terrain, getCamera());
        terrain.addControl(lodControl);
        terrain.addControl(new RigidBodyControl(0));

        rootNode.attachChild(terrain);

        getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "hellojme3/sky.jpg", SkyFactory.EnvMapType.EquirectMap));

        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setFallSpeed(10000);
        player.setPhysicsLocation(new Vector3f(-100, 0, 00));
        player.setGravity(3000f);

        // Creamos un objeto Spatial para el modelo del personaje
        Spatial playerModel = assetManager.loadModel("hellojme3/pato2.obj");
        playerModel.setLocalScale(1f);
        playerModel.addControl(player);
        //playerModel.setLocalTranslation(200,0,0);

        rootNode.attachChild(playerModel);

        bulletAppState.getPhysicsSpace().add(terrain);
        bulletAppState.getPhysicsSpace().add(player);

        ChaseCamera chaseCam = new ChaseCamera(cam, playerModel, inputManager);
        chaseCam.setDefaultDistance(500);
        chaseCam.setMaxDistance(1000);
        chaseCam.setMinDistance(2);

        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(.5f, -.5f, -.5f).normalizeLocal());
        rootNode.addLight(sun);

        Spatial structure = assetManager.loadModel("hellojme3/laberinto.obj");
        rootNode.attachChild(structure);
        structure.setLocalTranslation(1000, 0, 0);
        structure.setLocalScale(2f, 4f, 2f);
        
        //Crear la malla de colisión
        //Mesh mesh = ((Node) structure).getChild("hellojme3/laberinto.obj").getMesh(); // Obtener la malla del modelo
        //CollisionShape collisionShape = new MeshCollisionShape(mesh); // Crear la malla de colisión
        RigidBodyControl rigidBody = new RigidBodyControl(0);
        structure.addControl(rigidBody);
        //terrain.addControl(rigidBody);
        //playerModel.addControl(rigidBody);
        bulletAppState.getPhysicsSpace().add(rigidBody);

        Texture texture = assetManager.loadTexture("hellojme3/stone.jpeg");
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", texture);
        structure.setMaterial(material);

        Texture texture2 = assetManager.loadTexture("hellojme3/creeper.png");
        Material material2 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material2.setTexture("DiffuseMap", texture2);
        playerModel.setMaterial(material2);
    }

    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
            if (value) {
                left = true;
            } else {
                left = false;
            }
        } else if (binding.equals("Right")) {
            if (value) {
                right = true;
            } else {
                right = false;
            }
        } else if (binding.equals("Up")) {
            if (value) {
                up = true;
            } else {
                up = false;
            }
        } else if (binding.equals("Down")) {
            if (value) {
                down = true;
            } else {
                down = false;
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(2f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(2f);
        walkDirection.set(0, 0, 0);
        vision.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
            vision.addLocal(camDir);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
            vision.addLocal(camDir.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);

        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
            //vision.addLocal(camLeft.negate());
        }
        player.setWalkDirection(walkDirection);
        player.setViewDirection(vision);
        cam.setLocation(player.getPhysicsLocation());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent evt) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
