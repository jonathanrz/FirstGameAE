package jrz.games.andengine;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.DelayModifier;
import org.anddev.andengine.entity.modifier.LoopEntityModifier;
import org.anddev.andengine.entity.modifier.MoveByModifier;
import org.anddev.andengine.entity.modifier.MoveXModifier;
import org.anddev.andengine.entity.modifier.ParallelEntityModifier;
import org.anddev.andengine.entity.modifier.RotationModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.AutoParallaxBackground;
import org.anddev.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Display;
import android.view.KeyEvent;
import android.widget.Toast;

public class MainActivity extends BaseGameActivity implements IOnSceneTouchListener{
	// ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
	
	private Camera mCamera;
	private Scene mMainScene;
	
	private TextureRegion mPausedTextureRegion;
	private CameraScene mPauseScene;

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private Player mHero;

	private BitmapTextureAtlas mSheetBitmapTextureAtlas;
	private TiledTextureRegion mTargetTextureRegion;

	private LinkedList<AnimatedSprite> mTargetLL;
	private LinkedList<AnimatedSprite> mTargetsToBeAdded;
	
	private LinkedList<Sprite> mProjectileLL;
	private LinkedList<Sprite> mProjectilesToBeAdded;
	private TextureRegion mProjectileTextureRegion;
	
	private Sound mShootingSound;
	private Music mBackgroundMusic;
	
	private CameraScene mResultScene;
	private boolean mRunningFlag = false;
	private boolean mPauseFlag = false;
	private BitmapTextureAtlas mFontTexture;
	private Font mFont;
	private ChangeableText mScore;
	private int mHitCount;
	private final int mMaxScore = 10;
	private Sprite mWinSprite;
	private Sprite mFailSprite;
	private TextureRegion mWinTextureRegion;
	private TextureRegion mFailTextureRegion;
	private ProjectilesPool mProjPool;
	private TargetsPool mTargetsPool;
	
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	private TextureRegion mParallaxLayer;

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public Engine onLoadEngine() {
    	final Display display = getWindowManager().getDefaultDisplay();
    	int cameraWidth = display.getWidth();
    	int cameraHeight = display.getHeight();

    	mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
    	final Engine engine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera).setNeedsMusic(true).setNeedsSound(true));
    	
    	// enabling MultiTouch if available
    	try {
    	    if (MultiTouch.isSupported(this)) {
    	        engine.setTouchController(new MultiTouchController());
    	    } else {
    	    	Toast.makeText(this,"Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)",Toast.LENGTH_LONG).show();
    	    }
    	} catch (final MultiTouchException e) {
    	    Toast.makeText(this,"Sorry your Android Version does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)",Toast.LENGTH_LONG).show();
    	}

    	return engine;
    }

    @Override
    public void onLoadResources() {
    	mBitmapTextureAtlas				= new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
    	mAutoParallaxBackgroundTexture	= new BitmapTextureAtlas(1024, 1024,TextureOptions.DEFAULT);
    	mSheetBitmapTextureAtlas = new BitmapTextureAtlas(2048, 512);
    	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
    	mEngine.getTextureManager().loadTexture(mBitmapTextureAtlas);

    	mTargetTextureRegion		= BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mSheetBitmapTextureAtlas, this,"zombie_50pix.png", 0, 0, 3, 1);
    	mProjectileTextureRegion	= BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "bullet_100pix.png", 64, 0);
    	mPausedTextureRegion		= BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "paused.png", 0, 64);
    	mWinTextureRegion			= BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "win.png", 0, 128);
    	mFailTextureRegion			= BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "fail.png", 0, 256);
    	mParallaxLayer				= BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this,"background.png", 0, 0);
    	
    	mHero = new Player (this, mSheetBitmapTextureAtlas, mCamera);

    	mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
    	mFont = new Font(mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 40, true, Color.BLACK);
    	mEngine.getTextureManager().loadTexture(mFontTexture);
    	mEngine.getFontManager().loadFont(mFont);

    	SoundFactory.setAssetBasePath("mfx/");
    	try {
    		mShootingSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "pew_pew_lei.wav");
    	} catch (IllegalStateException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}

    	MusicFactory.setAssetBasePath("mfx/");

    	try {
    		mBackgroundMusic = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), this, "background_music.wav");
    		mBackgroundMusic.setLooping(true);
    	} catch (IllegalStateException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    	
    	mProjPool = new ProjectilesPool(mProjectileTextureRegion);
    	mTargetsPool = new TargetsPool(mTargetTextureRegion, mCamera);
    	
    	mEngine.getTextureManager().loadTexture(mSheetBitmapTextureAtlas);
    }

    @Override
    public Scene onLoadScene() {
    	mEngine.registerUpdateHandler(new FPSLogger());

    	mMainScene = new Scene();
    	// background preperations
    	final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 10);
    	autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-25.0f, new Sprite(0, mCamera.getHeight() - this.mParallaxLayer.getHeight(), this.mParallaxLayer)));
    	mMainScene.setBackground(autoParallaxBackground);

    	mMainScene.attachChild(mHero);
    	mMainScene.registerTouchArea(mHero);
    	mMainScene.setTouchAreaBindingEnabled(true);
    	
    	mTargetLL = new LinkedList<AnimatedSprite>();
    	mTargetsToBeAdded = new LinkedList<AnimatedSprite>();
    	
    	mProjectileLL = new LinkedList<Sprite>();
    	mProjectilesToBeAdded = new LinkedList<Sprite>();

    	createSpriteSpawnTimeHandler();
    	
    	IUpdateHandler detect = new IUpdateHandler() {
    		@Override
    		public void reset() {
    		}

    		@Override
    		public void onUpdate(float pSecondsElapsed) {
    			Iterator<AnimatedSprite> targets = mTargetLL.iterator();
    			AnimatedSprite _target = null;
    			boolean hit = false;

    			while (targets.hasNext()) {
    				_target = targets.next();

    				if (_target.getX() <= -_target.getWidth()) {
    					fail();
    					mTargetsPool.recyclePoolItem(_target);
    					targets.remove();
    					break;
    				}

    				Iterator<Sprite> projectiles = mProjectileLL.iterator();
    				Sprite _projectile;
    				
    				while (projectiles.hasNext()) {
    					_projectile = projectiles.next();

    					if (_projectile.getX() >= mCamera.getWidth() || _projectile.getY() >= mCamera.getHeight() + _projectile.getHeight() || _projectile.getY() <= -_projectile.getHeight()) {
    						mProjPool.recyclePoolItem(_projectile);
    						projectiles.remove();
    						continue;
    					}

    					if (_target.collidesWith(_projectile)) {
    						mProjPool.recyclePoolItem(_projectile);
    						projectiles.remove();
    						hit = true;
    						break;
    					}
    				}

    				if (hit) {
    					mHitCount++;
    					mScore.setText(String.valueOf(mHitCount));

    					mTargetsPool.recyclePoolItem(_target);
    					targets.remove();
    					hit = false;
    				}
    			}
    			
    			if (mHitCount >= mMaxScore) {
    				win();
    			}

    			mProjectileLL.addAll(mProjectilesToBeAdded);
    			mProjectilesToBeAdded.clear();

    			mTargetLL.addAll(mTargetsToBeAdded);
    			mTargetsToBeAdded.clear();
    		}
    	};

    	mMainScene.registerUpdateHandler(detect);
    	mMainScene.setOnSceneTouchListener(this);
    	
    	mPauseScene = new CameraScene(mCamera);
    	final int x = (int) (mCamera.getWidth() / 2 - mPausedTextureRegion.getWidth() / 2);
    	final int y = (int) (mCamera.getHeight() / 2 - mPausedTextureRegion.getHeight() / 2);
    	final Sprite pausedSprite = new Sprite(x, y, mPausedTextureRegion);
    	mPauseScene.attachChild(pausedSprite);
    	mPauseScene.setBackgroundEnabled(false);
    	
    	mResultScene = new CameraScene(mCamera);
    	mWinSprite = new Sprite(x, y, mWinTextureRegion);
    	mFailSprite = new Sprite(x, y, mFailTextureRegion);
    	mResultScene.attachChild(mWinSprite);
    	mResultScene.attachChild(mFailSprite);
    	mResultScene.setBackgroundEnabled(false);

    	mWinSprite.setVisible(false);
    	mFailSprite.setVisible(false);
    	mScore = new ChangeableText(0, 0, mFont, String.valueOf(mMaxScore));
    	mScore.setPosition(mCamera.getWidth() - mScore.getWidth() - 5, 5);
    	
    	restart();

    	return mMainScene;
    }

    @Override
    public void onLoadComplete() {
    }
    
    @Override
    public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
    	if (pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
    		if (mEngine.isRunning() && mBackgroundMusic.isPlaying()) {
    			pauseMusic();
    			mPauseFlag = true;
    			pauseGame();
    			Toast.makeText(this, "Menu button to resume", Toast.LENGTH_SHORT).show();
    		} else {
    			if (!mBackgroundMusic.isPlaying()) {
    				unPauseGame();
    				mPauseFlag = false;
    				resumeMusic();
    				mEngine.start();
    			}
    			return true;
    		}
    	}
		else if (pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if (!mEngine.isRunning() && mBackgroundMusic.isPlaying()) {
				mMainScene.clearChildScene();
				mEngine.start();
				restart();
				return true;
			}
		}
		
   		return super.onKeyDown(pKeyCode, pEvent);
    }
    
    @Override
    public void onResumeGame() {
        super.onResumeGame();

        if (mRunningFlag) {
        	if (mPauseFlag) {
        		mPauseFlag = false;
        		Toast.makeText(this, "Menu button to resume",Toast.LENGTH_SHORT).show();
            } else {
            	resumeMusic();
            	mEngine.stop();
            }
        } else {
        	mRunningFlag = true;
        }
    }
    
    @Override
    protected void onPause() {
        if (mRunningFlag) {
        	pauseMusic();

        	if (mEngine.isRunning()) {
        		pauseGame();
        		mPauseFlag = true;
            }
        }
        
        super.onPause();
    }
    
    // ===========================================================
    // Methods
    // ===========================================================
    
    public void addTarget() {
    	Random rand = new Random();
    	AnimatedSprite target = mTargetsPool.obtainPoolItem();
    	mMainScene.attachChild(target);

    	int minDuration = 2;
    	int maxDuration = 4;
    	int rangeDuration = maxDuration - minDuration;
    	int actualDuration = rand.nextInt(rangeDuration) + minDuration;

    	MoveXModifier mod = new MoveXModifier(actualDuration, target.getX(), -target.getWidth());
    	target.registerEntityModifier(mod.deepCopy());
    	target.animate(300);

    	mTargetsToBeAdded.add(target);
    }
    
    private void createSpriteSpawnTimeHandler() {
    	TimerHandler spriteTimerHandler;
    	float mEffectSpawnDelay = 1f;

    	spriteTimerHandler = new TimerHandler(mEffectSpawnDelay, true, new ITimerCallback() {
    		@Override
    		public void onTimePassed(TimerHandler pTimerHandler) {
    			addTarget();
    		}
    	});

    	getEngine().registerUpdateHandler(spriteTimerHandler);
    }
    
    private void shootProjectile(final float pX, final float pY) {
    	/*if(CoolDown.sharedCoolDown().checkValidity()){
    		return;
    	}*/

    	int offX = (int) (pX - (mHero.getX() + mHero.getWidth()));
    	int offY = (int) (pY - (mHero.getY() + mHero.getHeight()/3));
    	if (offX <= 0)
    		return;

    	final Sprite projectile = mProjPool.obtainPoolItem();
    	projectile.setPosition(mHero.getX() + mHero.getWidth(), mHero.getY());
    	
    	int realX = (int) (mCamera.getWidth() - (mHero.getX() + mHero.getWidth()) + projectile.getWidth());
    	float ratio = (float) realX / (float) offX;
    	int realY = (int) ((offY * ratio));
    	
    	float length = (float) Math.sqrt((realX * realX) + (realY * realY));
    	float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
    	float realMoveDuration = length / velocity;
    	
    	DelayModifier dMod = new DelayModifier(0.55f);
    	
    	dMod.addModifierListener(new IModifierListener<IEntity>() {
    		@Override
    		public void onModifierStarted(IModifier<IEntity> arg0, IEntity arg1) {
    		}

    		@Override
    		public void onModifierFinished(IModifier<IEntity> arg0, IEntity arg1) {
    			mShootingSound.play();
    			projectile.setVisible(true);
    			projectile.setPosition(mHero.getX() + mHero.getWidth(), mHero.getY()+ mHero.getHeight() / 3);
    			mProjectilesToBeAdded.add(projectile);
    		}
    	});

    	MoveByModifier mod = new MoveByModifier(realMoveDuration, realX, realY);
    	projectile.registerEntityModifier(mod.deepCopy());
    	
    	LoopEntityModifier loopMod = new LoopEntityModifier(new RotationModifier(0.5f, 0, -360));
    	final ParallelEntityModifier par = new ParallelEntityModifier(mod, loopMod);

    	projectile.registerEntityModifier(par.deepCopy());
    	
    	SequenceEntityModifier seq = new SequenceEntityModifier(dMod, par);
    	projectile.registerEntityModifier(seq);
    	projectile.setVisible(false);
    	mMainScene.attachChild(projectile,1);
    	
    	mHero.animate(50, false);
    }

    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
    	if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
    		final float touchX = pSceneTouchEvent.getX();
    		final float touchY = pSceneTouchEvent.getY();
    		shootProjectile(touchX, touchY);
    		return true;
    	}

    	return false;
    }
    
    public void pauseGame() {
    	mMainScene.setChildScene(mPauseScene, false, true, true);
    	mEngine.stop();
    }

    public void unPauseGame() {
    	mMainScene.clearChildScene();
    	mEngine.start();
    }

    public void pauseMusic() {
    	if(mRunningFlag)
	    	if (mBackgroundMusic.isPlaying())
	    		mBackgroundMusic.pause();
    }

	public void resumeMusic() {
		if(mRunningFlag)
			if (!mBackgroundMusic.isPlaying())
				mBackgroundMusic.resume();
	}
	
	public void restart() {
		runOnUpdateThread(new Runnable() {

			@Override
			public void run() {
				mMainScene.detachChildren();
				mMainScene.attachChild(mHero, 0);
				mMainScene.attachChild(mScore);
			}
		});

		mHitCount = 0;
		mScore.setText(String.valueOf(mHitCount));
		mProjectileLL.clear();
		mProjectilesToBeAdded.clear();
		mTargetsToBeAdded.clear();
		mTargetLL.clear();
	}
	
	public void fail() {
		/*if (mEngine.isRunning()) {
			mWinSprite.setVisible(false);
			mFailSprite.setVisible(true);
			mMainScene.setChildScene(mResultScene, false, true, true);
			mEngine.stop();
		}*/
	}

	public void win() {
		if (mEngine.isRunning()) {
			mFailSprite.setVisible(false);
			mWinSprite.setVisible(true);
			mMainScene.setChildScene(mResultScene, false, true, true);
			mEngine.stop();
		}
	}

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}