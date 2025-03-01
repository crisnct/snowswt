package org.herbshouse.logic.fractals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.herbshouse.gui.GuiUtils;
import org.herbshouse.gui.SWTResourceManager;
import org.herbshouse.logic.AbstractGenerator;
import org.herbshouse.logic.GraphicalImageGenerator;
import org.herbshouse.logic.Point2D;
import org.herbshouse.logic.Utils;
import org.herbshouse.logic.fractals.visitor.TreeBranchesVisitor;
import org.herbshouse.logic.fractals.visitor.TreeVisitorData;

public class FractalsGenerator extends AbstractGenerator<Tree> implements GraphicalImageGenerator {

  public static final double IMPERFECTTION_FACTOR = 0.07;
  private final Map<TreeType, Tree> trees = new HashMap<>();
  private final TreeBranchesVisitor treeBranchesVisitor = new TreeBranchesVisitor();
  private boolean shutdown = false;
  private double counterWind = 0;
  private int counterWindDir = 1;
  private Image image;

  @Override
  public void run() {
    for (TreeType val : TreeType.values()) {
      trees.put(val, generateTree(val));
    }
    while (!shutdown) {
      if (config.isFractals()) {
        if (!getFlagsConfiguration().isPause()) {
          if (this.isBendingTree()) {
            trees.put(config.getFractalsType(), generateTree(config.getFractalsType()));
          } else {
            Tree tree = trees.get(config.getFractalsType());
            if (!treeBranchesVisitor.isVisiting(tree)) {
              treeBranchesVisitor.startVisiting(tree);
            }
          }
          this.createImage();
          Utils.sleep(getSleepDuration());
        } else {
          Utils.sleep(100);
        }
      } else {
        Utils.sleep(getSleepDurationDoingNothing());
      }
    }
    if (image != null && !image.isDisposed()) {
      image.dispose();
    }
    treeBranchesVisitor.shutdown();
  }

  private boolean isBendingTree() {
    return config.isNormalWind()
        && (config.getFractalsType() == TreeType.PERFECT_DEFAULT || config.getFractalsType() == TreeType.PERFECT_FIR);
  }

  public void createImage() {
    if (image != null && !image.isDisposed()) {
      image.dispose();
    }
    Display display = new Display();
    PaletteData palette = new PaletteData(0xFF0000, 0x00FF00, 0x0000FF);
    ImageData imageData = new ImageData(screenBounds.width, screenBounds.height, 8, palette);
    Image imageNew = new Image(display, imageData);
    GC newGC = new GC(imageNew);
    newGC.setAdvanced(true);
    newGC.setAntialias(SWT.ON);
    newGC.setLineCap(SWT.CAP_ROUND);
    for (Tree tree : getMoveableObjects()) {
      TreeVisitorData visitorData = (TreeVisitorData) tree.getData("TreeFillData");
      this.draw(newGC, tree, visitorData);
    }
    newGC.dispose();
    display.dispose();
    image = imageNew;
  }

  @Override
  public Image getImage() {
    return config.isFractals() ? image : null;
  }

  private void draw(GC gc, ITree tree, TreeVisitorData visitorData) {
    Color foregroundColor = SWTResourceManager.getColor(new RGB(255, 255, 255));
    if (visitorData != null && !isBendingTree()) {
      if (visitorData.isVisitedBranch(tree)) {
        foregroundColor = SWTResourceManager.getColor(new RGB(0, 255, 0));
      } else {
        return;
      }
    }
    gc.setForeground(foregroundColor);
    GuiUtils.drawLine(gc, tree.getStart(), tree.getEnd(), tree.getThickness());
    for (TreeBranch branch : tree.getBranches()) {
      draw(gc, branch, visitorData);
    }
  }

  @Override
  protected int getSleepDurationDoingNothing() {
    return 5000;
  }

  @Override
  public void switchBlackHoles() {

  }

  @Override
  public void switchIndividualMovements() {

  }

  @Override
  public boolean canControllerStart() {
    return true;
  }

  @Override
  public void switchGraphicalSounds() {

  }

  @Override
  protected int getSleepDuration() {
    return 10;
  }

  @Override
  public void turnOnHappyWind() {

  }

  @Override
  public void switchDebug() {

  }

  @Override
  public void mouseMove(Point2D mouseLocation) {

  }

  @Override
  public void mouseDown(int button, Point2D mouseLocation) {

  }

  @Override
  public void mouseScrolled(int count) {

  }

  @Override
  public void reset() {

  }

  private Tree generateTree(TreeType type) {
    Tree tree = new Tree();
    tree.setLocation(new Point2D(screenBounds.width * 0.24, -250));
    tree.setSize(260);
    tree.setThickness(20);
    tree.setAngle(Math.PI / 2);

    if (counterWind > 0.9 || counterWind < 0) {
      counterWindDir *= -1;
    }
    counterWind += 0.01 * counterWindDir;

    switch (type) {
      case PERFECT_DEFAULT:
        this.generateBranchesDefault(tree, true);
        break;
      case PERFECT_FIR:
        this.generateBranchesFir(tree, true);
        break;
      case RANDOM_DEFAULT:
        this.generateBranchesDefault(tree, false);
        break;
      case RANDOM_FIR:
        this.generateBranchesFir(tree, false);
        break;
    }
    return tree;
  }

  @Override
  public void changedFractalType() {
    trees.put(config.getFractalsType(), generateTree(config.getFractalsType()));
    counterWind = 0;
  }

  private void generateBranchesFir(ITree parent, boolean perfect) {
    double parentLength = parent.getLength();
    double mainBranchLength = parentLength * 0.8;
    if (mainBranchLength <= 1) {
      return;
    }

    double angleMainBranch = parent.getAngle();
    double angleLeftBranch = Utils.normAngle(angleMainBranch + Math.toRadians(110));
    double angleRightBranch = Utils.normAngle(angleMainBranch - Math.toRadians(110));
    if (!perfect) {
      angleMainBranch *= 1 + angleMainBranchIncrease();
    }
    if (config.isNormalWind()) {
      angleMainBranch -= counterWind;
    }

    TreeBranch mainBranch = new TreeBranch();
    mainBranch.setStart(parent.getEnd());
    mainBranch.setEnd(Utils.moveToDirection(mainBranch.getStart(), mainBranchLength, angleMainBranch));
    mainBranch.setThickness(Math.max(parent.getThickness() * 0.7, 1));
    parent.addBranch(mainBranch);

    parent.addBranch(this.generateBranch(mainBranch, 0.8, angleLeftBranch, perfect));
    parent.addBranch(this.generateBranch(mainBranch, 0.8, angleRightBranch, perfect));

    this.generateBranchesFir(mainBranch, perfect);
  }

  private double angleMainBranchIncrease() {
    return IMPERFECTTION_FACTOR * (2 * Math.random() - 1);
  }

  @SuppressWarnings("SameParameterValue")
  private TreeBranch generateBranch(TreeBranch mainBranch, double locOnMainBranch, double angle, boolean perfect) {
    TreeBranch branch = new TreeBranch();
    double mainBranchLength = mainBranch.getLength();
    branch.setStart(Utils.moveToDirection(mainBranch.getStart(), mainBranchLength * locOnMainBranch, mainBranch.getAngle()));
    branch.setEnd(Utils.moveToDirection(branch.getStart(), mainBranchLength * 0.4, angle));
    branch.setThickness(Math.max(mainBranch.getThickness() * 0.7, 1));
    this.generateBranchesFir(branch, perfect);
    return branch;
  }

  private void generateBranchesDefault(ITree parent, boolean perfect) {
    double parentLength = parent.getLength();
    double mainBranchLength = parentLength * 0.8;
    if (mainBranchLength <= 1) {
      return;
    }

    double angleMainBranch = parent.getAngle();
    double angleLeftBranch = angleMainBranch + Math.PI / 4;
    double angleRightBranch = angleMainBranch - Math.PI / 4;
    if (!perfect) {
      angleMainBranch *= 1 + angleMainBranchIncrease();
    }

    if (config.isNormalWind()) {
      angleMainBranch -= counterWind;
    }

    TreeBranch mainBranch = new TreeBranch();
    mainBranch.setStart(parent.getEnd());
    mainBranch.setEnd(Utils.moveToDirection(mainBranch.getStart(), mainBranchLength, angleMainBranch));
    mainBranch.setThickness(Math.max(parent.getThickness() * 0.7, 1));
    parent.addBranch(mainBranch);

    TreeBranch leftBranch = new TreeBranch();
    leftBranch.setStart(Utils.moveToDirection(mainBranch.getStart(), mainBranchLength * 0.7, angleMainBranch));
    leftBranch.setEnd(Utils.moveToDirection(leftBranch.getStart(), mainBranchLength * 0.4, angleLeftBranch));
    leftBranch.setThickness(Math.max(parent.getThickness() * 0.3, 1));
    parent.addBranch(leftBranch);

    TreeBranch rightBranch = new TreeBranch();
    rightBranch.setStart(Utils.moveToDirection(mainBranch.getStart(), mainBranchLength * 0.3, angleMainBranch));
    rightBranch.setEnd(Utils.moveToDirection(rightBranch.getStart(), mainBranchLength * 0.4, angleRightBranch));
    rightBranch.setThickness(Math.max(parent.getThickness() * 0.3, 1));
    parent.addBranch(rightBranch);

    this.generateBranchesDefault(mainBranch, perfect);
    this.generateBranchesDefault(leftBranch, perfect);
    this.generateBranchesDefault(rightBranch, perfect);
  }

  @Override
  public List<Tree> getMoveableObjects() {
    if (config.isFractals()) {
      return Collections.singletonList(trees.get(config.getFractalsType()));
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public void shutdown() {
    shutdown = true;
  }

  @Override
  public void provideImageData(ImageData imageData) {

  }

  @Override
  public void switchAttack() {

  }

  @Override
  public void changeAttackType(int oldType, int newType) {

  }

}
