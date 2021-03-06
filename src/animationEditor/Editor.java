package animationEditor;

import java.io.IOException;
import java.util.ArrayList;

import processing.core.PApplet;
import game.Body;
import game.GraphicObject;
import animationEditor.KeyFrame;


public class Editor extends PApplet implements game.ApplicationConstants {
	
	private Body body;
	
	private Menu editAnimUI;
	private Menu playAnimUI;
	private Menu timeSelectUI;
	private Menu startUI;
	
	private final float ANGLE_INCR = PI/16;
	private boolean animate = false;
	private float lastTime;
	private long frame = 0L;
	private ArrayList<KeyFrame> keyframes;
	//private KeyFrame start;
	private int limbSelected;
	private float buttonY = WORLD_Y_MIN+6f;
	private int jointsOnLimbs = 2;
	private int limbsOnBody = 2;
	private int timeSelected = 1;
	private boolean editAnimation = false;
	private boolean startScreen = false;
	private float defaultFrameLength = 0.5f;
	
	
	public void settings() 
	{
		size(WINDOW_WIDTH, WINDOW_HEIGHT);
	}

	public void setup() 
	{
		setupGraphicClasses_();
		initStartUI();
	}

	
	public void draw() 
	{	
		frame++;
		if (frame % 5 == 0) {
			background(167);
			
			pushMatrix();
			
			translate(ORIGIN_X, ORIGIN_Y);
	 		
	 		scale(WORLD_TO_PIXELS_SCALE, -WORLD_TO_PIXELS_SCALE);	
			
	 		// horizontal line for the "ground"
			stroke(255, 0, 0);
			strokeWeight(0.1f);
			line(WORLD_X_MIN, 0, WORLD_X_MAX, 0);
			line(0, WORLD_Y_MIN, 0, WORLD_Y_MAX);
			fill(0, 255, 255);
			ellipse(0, 0, 1, 1);
			
			if(startScreen)
				startUI.draw();
			else {
				body.draw();
				if(editAnimation) {
					editAnimUI.draw();
				}else
					playAnimUI.draw();
				
				timeSelectUI.draw();
			}
			popMatrix();
		}
		
		float t = millis();
		
		if (animate)
		{
			
			//	time in seconds since last update: (t-lastTime_)*0.001f
			float dt = (t-lastTime)*0.001f;
			
			if(!body.isAnimDone()) {
				// update the time keeper button
				updateTime(t/1000f);
				body.update(dt);
			}else {
				animate = false;
				println("Animation complete.");
			}
		}

		lastTime = t;
		
	}
	
	/**
	 * Selecting a limb for editing with keyboard controls
	 * @param index
	 */
	public void selectLimb(int index) {
		body.getLimbs().get(limbSelected).deselect();
		limbSelected = index;
		println("Limb "+index+ " selected!");
		body.getLimbs().get(limbSelected).select();
	}
	
	public void keyReleased() 
	{
		// make sure to only allow keyboard control in edit mode.
		if(editAnimation) {
			switch (key) {
			//--------------------------------------
			//	Forward Kinematics
			//--------------------------------------
			
			case 'b':
				FileInOutMachine.saveKeyFramesToFile(keyframes, limbsOnBody, jointsOnLimbs);
				break;
			case 'n':
				snapCurrent();
				break;
			case 'w':
				body.moveUp();
				break;
			case 'a':
				body.moveLeft();
				break;
			case 's':
				body.moveDown();
				break;
			case 'd':
				body.moveRight();
				break;
			case 'q':
				body.rotate(ANGLE_INCR);
				break;
			case 'e':
				body.rotate(-ANGLE_INCR);
				break;
			case 'z':
				body.getLimbs().get(limbSelected).rotateJoint(0, ANGLE_INCR);
				break;
			case 'x':
				body.getLimbs().get(limbSelected).rotateJoint(0, -ANGLE_INCR);
				break;
			case 'c':
				body.getLimbs().get(limbSelected).rotateJoint(1, ANGLE_INCR);
				break;
			case 'v':
				body.getLimbs().get(limbSelected).rotateJoint(1, -ANGLE_INCR);
				break;
			case '0':
				selectLimb(0);
				break;
			case'1':
				selectLimb(1);
				break;
			case '2':
				selectLimb(2);
				break;
			case '3':
				selectLimb(3);
				break;
			}
		}
	}

	public void mouseReleased() {
		float mouseXW = PIXELS_TO_WORLD_SCALE * (mouseX - ORIGIN_X);
		float mouseYW = -PIXELS_TO_WORLD_SCALE * (mouseY - ORIGIN_Y);
		
		if(startScreen)
			startUI.checkIsInside(mouseXW, mouseYW);
		else {
			if(editAnimation) {
				editAnimUI.checkIsInside(mouseXW, mouseYW);
			}else
				playAnimUI.checkIsInside(mouseXW, mouseYW);
		}
		
	}
	
	/**
	 * This initalizes the menu that shows when you first run the application.
	 */
	public void initStartUI() {
		startScreen = true;
		ArrayList<String> buttonNames = new ArrayList<String>();
		ArrayList<Runnable> r = new ArrayList<Runnable>(); 
		buttonNames.add(" New  ");
		buttonNames.add(" Open  ");
		r.add(new Runnable() { public void run() {newAnim();}});
		r.add(new Runnable() { public void run() {
			try {
				keyframes = FileInOutMachine.getKeyframesFromFile("default.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initializeEverything();
			startScreen = false;
			}});
		startUI = new Menu(r, buttonNames, buttonY-3f, WORLD_WIDTH/4, 2f, WORLD_X_MIN);
	}

	/**
	 * These buttons also have keyboard control counterparts, so they use the same methods
	 * as some keyboard controls.
	 */
	public void initAnimUI() {
		ArrayList<Runnable> r = new ArrayList<Runnable>();
		r.add(new Runnable() { public void run() {snapCurrent();}});
		r.add(new Runnable() { public void run() {FileInOutMachine.saveKeyFramesToFile(keyframes, limbsOnBody, jointsOnLimbs);}});
		//r.add(new Runnable() { public void run() {decreaseTimeVal();}});
		//r.add(new Runnable() { public void run() {increaseTimeVal();}});
		r.add(new Runnable() { public void run() {playAnim(); resetAnim();}});
		
		ArrayList<String> buttonNames = new ArrayList<String>();
		// There are extra spaces so that the text gets seen as wider and scales the text size
		//  appropriately.
		buttonNames.add("  Snapshot  ");
		buttonNames.add("  Save All  ");
		//buttonNames.add(" <  ");
		//buttonNames.add("  > ");
		buttonNames.add("   Play  ");

		editAnimUI = new Menu(r, buttonNames, buttonY, WORLD_WIDTH, 2f, WORLD_X_MIN);
	}
	
	/**
	 * The menu that shows in play mode has its functions and button names made here.
	 */
	public void initPlayUI(){
		ArrayList<Runnable> r = new ArrayList<Runnable>();
		r.add(new Runnable() { public void run() {playAnim();}});
		r.add(new Runnable() { public void run() {pauseAnim();}});
		r.add(new Runnable() { public void run() {resetAnim();}});
		r.add(new Runnable() { public void run() {newAnim();}});
		r.add(new Runnable() { public void run() {editAnim();}});
		
		ArrayList<String> buttonNames = new ArrayList<String>();
		buttonNames.add("Play");
		buttonNames.add("Pause");
		buttonNames.add("Restart");
		buttonNames.add("New");
		buttonNames.add("Edit");
		
		playAnimUI = new Menu(r, buttonNames, buttonY, WORLD_WIDTH/2, 2f, WORLD_X_MIN);
	}
	
	/**
	 * This menu only shows the time value of the animation, but it needs to be shown in
	 * multiple modes, so the button gets its own menu
	 */
	public void initTimeSelectUI() {
		ArrayList<Runnable> r = new ArrayList<Runnable>();
		r.add(new Runnable() { public void run() {blah();}});
		
		ArrayList<String> buttonNames = new ArrayList<String>();
		buttonNames.add("Time " + timeSelected);
		
		timeSelectUI = new Menu(r, buttonNames, WORLD_Y_MIN+10f, WORLD_WIDTH/8, 2f, WORLD_X_MAX-10f);
	}
	
	/**
	 * update the timekeeping button
	 * @param time
	 */
	public void updateTime(float time) {
		timeSelectUI.get(0).changeText("  "+time);
	}	
	
	/**
	 * jump forward or backward in the animation to the selected frame
	 * @param frame
	 */
	public void jumpTo(int frame) {
		body.jumpTo(frame);
		println("Jumping to frame "+ frame);
		updateTime(keyframes.get(frame).getT());
	}
	
	//placeholder for what happens when you press the time-keeping button
	public void blah() {}
	
	/**
	 * If I want to add the ability to increment the time a keyframe gets made for (to make
	 * the change slower)
	 */
	public void increaseTimeVal() {
		if(timeSelected < 501)
			timeSelected++;
		updateTime(timeSelected);
	}
	
	/**
	 * If I want to add the ability to decrement the time a keyframe gets made for (to make
	 * the change faster)
	 */
	public void decreaseTimeVal() {
		if(timeSelected>keyframes.get((keyframes.size()-1)).getT())
			timeSelected--;
		updateTime(timeSelected);
	}
	
	/**
	 * Pressing play just enables animate and makes the editing menu disappear
	 */
	public void playAnim() {
		animate = true;
		editAnimation = false;
		println("playing");
	}
	
	/**
	 * restart the animation from the beginning
	 */
	public void resetAnim() {
		jumpTo(0);
		body.restartAnim();
		animate = true;
	}
	
	/**
	 * disable the animation playing.  Press play to begin it again from the point pause
	 * was pressed
	 */
	public void pauseAnim() {
		animate = false;
		println("paused");
	}
	
	/**
	 * Stop animating when editing and make the body go back to the first frame.
	 */
	public void editAnim() {
		//showJumpTo = true;
		editAnimation = true;
		animate = false;
		jumpTo(0);
	}
	
	/**
	 * Resets the saved keyframes and puts the body back at the starting point.
	 */
	public void newAnim() {
		animate = false;
		startScreen = false;
		editAnimation = true;
		initializeEverything();
	}
	
	public void initializeEverything() {
		body = new Body("default_spider.txt", limbsOnBody, jointsOnLimbs, 8);
		try {
			keyframes = FileInOutMachine.getKeyframesFromFile("Animations/default_spider.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		limbSelected = 1;
		selectLimb(0);
		lastTime = millis();
		initAnimUI();
		initPlayUI();
		initTimeSelectUI();
	}
	
	/**
	 * Adds the current position and orientation of the body and its limbs to the keyframe
	 * array and increments the time display
	 */
	public void snapCurrent() {
		keyframes.add(new KeyFrame(timeSelected, body.getX(), body.getY(), 
				body.getA(), body.getLimbAngles() ));
		println("Saved keyframe at "+ timeSelected+ " (seconds).");
		timeSelected+=defaultFrameLength;
		updateTime(timeSelected);
	}
	
	/**
	 * setup the graphic classes, namely graphic object
	 */
	public void setupGraphicClasses_()
	{
		if (GraphicObject.setup(this) != 1)
		{
			println("A graphic classe\'s setup() method was called illegally before this class");
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		PApplet.main("animationEditor.Editor");
	}
}
