import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class Game extends Canvas implements Runnable{
	
	public static final long serialVersionUID = 1L; 
	
    private boolean isRunning = false;
    
    private Thread thread;
    private Handler handler;
    private Camera camera;
    private SpriteSheet ss;
    
    private BufferedImage world = null;
    private BufferedImage sprite_sheet = null;
    private BufferedImage floor = null;
    
    public int ammo = 100;
    public int hp = 100;
    public int totalEnemy = 20;

    public Game() {
        new Window(1000, 563, "Wiz4rd Game", this);

        start();

        handler = new Handler();
        camera = new Camera(0, 0);
        
        this.addKeyListener(new KeyInput(handler));
        
        BufferedImageLoader loader = new BufferedImageLoader();
        world = loader.loadImage("/Wizardry_World.png");
        sprite_sheet = loader.loadImage("/Features_SpriteSheet.png");
        
        ss = new SpriteSheet(sprite_sheet);
        floor = ss.grabImage(4, 2, 32, 32);
        
        this.addMouseListener(new MouseInput(handler, camera, this, ss));

        loadLevel(world);
    }

    //start thread
    private void start() {
        isRunning = true;
        thread = new Thread(this);  //"this" as in the run method of the class
        thread.start();
    }

    //stop thread
    private void stop() {
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void run() {
        this.requestFocus();
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while(isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while(delta >= 1) {
                tick();
                //updates++;
                delta--;
            }
            render();
            frames++;

            if(System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                frames = 0;
                //updates = 0;
            }
        }
        stop();
    }

    //updates everything in game - 60 times a second
    public void tick() {
    		for(int i = 0; i < handler.object.size(); i++) {
    			if(handler.object.get(i).getId() == ID.Player) {
    				camera.tick(handler.object.get(i));
    			}
    		}
    		
        handler.tick();
    }

    //render ("draws") everything in game - couple thousand times a second
    public void render() {
        BufferStrategy bs = this.getBufferStrategy();
        
        if(bs == null) {
            this.createBufferStrategy(3);   //preloading frames before they are shown
            return;
        }

        Graphics g = bs.getDrawGraphics();
        Graphics2D g2d = (Graphics2D) g;
        //////////////////////////////////
        
        g2d.translate(-camera.getX(), -camera.getY());
        
        for(int xx = 0; xx < 30 * 72; xx+=32) {
        	for(int yy = 0; yy < 30 * 72; yy+=32) {
        		g.drawImage(floor, xx, yy, null);
        	}
        }

        handler.render(g);
        
        g2d.translate(camera.getX(), camera.getY());
        
        
        g.setColor(Color.gray);
        g.fillRect(5, 5, 200, 32);
        
        g.setColor(Color.red);
        g.fillRect(5, 5, hp*2, 32);
        if(hp == 0) {
        	g.setColor(Color.green);
        	g.drawString("Game Over!", 500, 250);
        }

        if (totalEnemy <= 0){
            g.setColor(Color.green);
            g.drawString("Game Over!", 500, 250);
        }
        
        g.setColor(Color.black);
        g.drawRect(5, 5, 200, 32);
        
        g.setColor(Color.orange);
        g.drawString("Spells: " + ammo, 5, 50);

        //////////////////////////////////
        g.dispose();
        bs.show();
    }
    
    //loading the level
    private void loadLevel(BufferedImage image) {
		int w = image.getWidth();
		int h = image.getHeight();

		for (int xx = 0; xx < w; xx++) {
			for (int yy = 0; yy < h; yy++) {
				int pixel = image.getRGB(xx, yy);

				int red = (pixel >> 16) & 0xff;
				int green = (pixel >> 8) & 0xff;
				int blue = (pixel) & 0xff;

				if(red == 255) {
					handler.addObject(new Block(xx * 32, yy * 32, ID.Block, ss));
				}
				if(blue == 255 && green == 0) {
					handler.addObject(new Wizard(xx * 32, yy * 32, ID.Player, handler, this, ss));
				}
				if(green == 255 && blue == 0) {
					handler.addObject(new Enemy(xx * 32, yy * 32, ID.Enemy, handler, ss,this));
				}
				if(green == 255 && blue == 255) {
					handler.addObject(new Crate(xx * 32, yy * 32, ID.Crate, ss));
				}
			}
		}
	}

    public static void main(String[] args) {
        new Game();
    }

}
