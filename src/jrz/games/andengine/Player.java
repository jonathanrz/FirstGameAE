package jrz.games.andengine;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

public class Player extends AnimatedSprite {
	public Player(BaseGameActivity activity, BitmapTextureAtlas sheetBitmapTextureAtlas, Camera camera) {
		super(0, 0, onLoadResources(activity, sheetBitmapTextureAtlas, camera));

		final float X = (getTextureRegion().getWidth() / 20);
    	final float Y = ((camera.getHeight() - getTextureRegion().getHeight()) / 2);
    	setPosition(X, Y);
	}

	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		this.setPosition(this.getX(), pSceneTouchEvent.getY() - this.getHeight() / 2);
		return true;
	}
	
	public static TiledTextureRegion onLoadResources(BaseGameActivity activity, BitmapTextureAtlas sheetBitmapTextureAtlas, Camera camera){
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		return BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(sheetBitmapTextureAtlas, activity,"hero_50.png", 0, 212, 11, 1);
	}
}
