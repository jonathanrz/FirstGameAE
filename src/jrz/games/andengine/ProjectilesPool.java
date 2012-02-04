package jrz.games.andengine;

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.pool.GenericPool;

public class ProjectilesPool extends GenericPool<Sprite> {
	 private TextureRegion mTextureRegion;

	 public ProjectilesPool(TextureRegion pTextureRegion) {
		 if (pTextureRegion == null) {
			 // Need to be able to create a Sprite so the Pool needs to have a TextureRegion
			 throw new IllegalArgumentException("The texture region must not be NULL");
		 }

		 mTextureRegion = pTextureRegion;
	 }

	 /** Called when a projectile is required but there isn't one in the pool */
	 @Override
	 protected Sprite onAllocatePoolItem() {
		 return new Sprite(0, 0, mTextureRegion.deepCopy());
	 }

	 /** Called when a projectile is sent to the pool */
	 protected void onHandleRecycleItem(final Sprite projectile) {
		 projectile.clearEntityModifiers();
		 projectile.clearUpdateHandlers();
		 projectile.setVisible(false);
		 projectile.detachSelf();
		 projectile.reset();
	 }
}
