package jrz.games.andengine;

import java.util.Random;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.pool.GenericPool;

public class TargetsPool extends GenericPool<AnimatedSprite> {
	private TiledTextureRegion mTextureRegion;
	private Camera mCamera;
	Random mRand;

	public TargetsPool(TiledTextureRegion TextureRegion, Camera camera) {
		if (TextureRegion == null) {
			 // Need to be able to create a Sprite so the Pool needs to have a TextureRegion
			 throw new IllegalArgumentException("The texture region must not be NULL");
		}
		 
		if (camera == null) {
			// Need to be able to create a Sprite so the Pool needs to have a TextureRegion
			throw new IllegalArgumentException("The texture region must not be NULL");
		}

		mTextureRegion = TextureRegion;
		mCamera = camera;
		mRand = new Random();
	}

	/** Called when a projectile is required but there isn't one in the pool */
	@Override
	protected AnimatedSprite onAllocatePoolItem() {
    	int x = (int) mCamera.getWidth() + mTextureRegion.getWidth();
    	int minY = mTextureRegion.getHeight();
    	int maxY = (int) (mCamera.getHeight() - mTextureRegion.getHeight());
    	int rangeY = maxY - minY;
    	int y = mRand.nextInt(rangeY) + minY;
	    	
    	return new AnimatedSprite(x,y, mTextureRegion.deepCopy());
	}

	/** Called when a projectile is sent to the pool */
	protected void onHandleRecycleItem(final AnimatedSprite target) {
		target.clearEntityModifiers();
		target.clearUpdateHandlers();
		target.setVisible(false);
		target.detachSelf();
		target.reset();
	}
}
