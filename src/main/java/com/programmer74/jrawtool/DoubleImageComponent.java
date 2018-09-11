package com.programmer74.jrawtool;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class DoubleImageComponent extends Component {

  private DoubleImage doubleImage;

  private double scale;
  private int paintX;
  private int paintY;
  private int paintW;
  private int paintH;

  private int pressedX = 0;
  private int pressedY = 0;

  private boolean doAutoScale = true;

  private Consumer<Object> afterPaintCallback;

  public DoubleImageComponent(final DoubleImage doubleImage) {
    this.doubleImage = doubleImage;
    this.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(final MouseEvent mouseEvent) {
        pressedX = mouseEvent.getX();
        pressedY = mouseEvent.getY();
      }
      @Override public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isConsumed()) {
          e.consume();
          //this is doubleclick handler
          setAutoScale();
        }
      }
    });

    this.addMouseMotionListener(new MouseMotionAdapter() {
      @Override public void mouseDragged(final MouseEvent mouseEvent) {
        doAutoScale = false;

        int deltaX = pressedX - mouseEvent.getX();
        int deltaY = pressedY - mouseEvent.getY();

        paintX = paintX - deltaX;
        paintY = paintY - deltaY;

        pressedX = mouseEvent.getX();
        pressedY = mouseEvent.getY();

        forceImageInsidePreviewPanel();
        mouseEvent.getComponent().repaint();
      }
    });

    this.addMouseWheelListener(new MouseWheelListener() {
      @Override public void mouseWheelMoved(final MouseWheelEvent mouseWheelEvent) {
        doAutoScale = false;
        double newScale = scale + mouseWheelEvent.getScrollAmount() * mouseWheelEvent.getWheelRotation() * -1.0 / 100;
        if (newScale < 0.1) newScale = 0.1;
        if (newScale > 2) newScale = 2;

//        System.out.println("New scale: " + newScale);
        setScaleByMouse(newScale, mouseWheelEvent.getX(), mouseWheelEvent.getY());
      }
    });

    recalculatePaintParams();
    doubleImage.subscribeToAfterChunkPaintedCallback((e) -> this.repaint());
  }

  public void setAfterPaintCallback(final Consumer<Object> afterPaintCallback) {
    this.afterPaintCallback = afterPaintCallback;
  }

  public void setScale(final double newScale) {
    setScaleByMouse(newScale, getWidth() / 2, getHeight() / 2);
    repaint();
  }

  public void setAutoScale() {
    setScale(calculateScale());
    centerPaintedImage();
    repaint();
  }

  private void centerPaintedImage() {
    int componentWidth = getWidth();
    int componentHeight = getHeight();

    paintX = (componentWidth - paintW) / 2;
    paintY = (componentHeight - paintH) / 2;

    this.doAutoScale = true;
  }

  private void forceImageInsidePreviewPanel() {
    if (paintW < getWidth()) {
      if (paintX < 0) paintX = 0;
      if ((paintX + paintW) > getWidth()) paintX = getWidth() - paintW;
    } else {
      if ((paintX + paintW) < getWidth()) paintX = getWidth() - paintW;
      if (paintX > 0) paintX = 0;
    }

    if (paintH < getHeight()) {
      if (paintY < 0) paintY = 0;
      if ((paintY + paintH) > getHeight()) paintY = getHeight() - paintH;
    } else {
      if ((paintY + paintH) < getHeight()) paintY = getHeight() - paintH;
      if (paintY > 0) paintY = 0;
    }
  }

  private void recalculatePaintParams() {
    int imageWidth = doubleImage.getWidth();
    int imageHeight = doubleImage.getHeight();

    if (doAutoScale) {
      scale = calculateScale();
    }

    Double screenImageWidth = imageWidth * scale;
    Double screenImageHeight = imageHeight * scale;

    paintW = screenImageWidth.intValue();
    paintH = screenImageHeight.intValue();

    if (doAutoScale) {
      centerPaintedImage();
    }
  }

  public void setScaleByMouse(final double newScale, final int cursorX, final int cursorY) {
//    System.out.println("Mouse cursor at " + cursorX + ":" + cursorY);

//    System.out.println("Current scale: " + scale + " new scale: " + newScale);

    double onImageX = (cursorX - paintX) * 1.0 / paintW;
    double onImageY = (cursorY - paintY) * 1.0 / paintH;
//    System.out.println("old onImageCursor at " + onImageX + " : " + onImageY);

    this.doAutoScale = false;
    this.scale = newScale;
    recalculatePaintParams();

    double newOnImageX = (cursorX - paintX) * 1.0 / paintW;
    double newOnImageY = (cursorY - paintY) * 1.0 / paintH;
//    System.out.println("new onImageCursor at " + newOnImageX + " : " + newOnImageY);

    double onImageXDelta = (newOnImageX - onImageX);
    double onImageYDelta = (newOnImageY - onImageY);
    paintX = paintX + (int)(paintW * onImageXDelta);
    paintY = paintY + (int)(paintH * onImageYDelta);

    forceImageInsidePreviewPanel();
    this.repaint();
  }

  public double getScale() {
    return scale;
  }

  public double calculateScale() {
    int imageWidth = doubleImage.getWidth();
    int imageHeight = doubleImage.getHeight();

    int componentWidth = getWidth();
    int componentHeight = getHeight();

    double wK = imageWidth * 1.0 / componentWidth;
    double hK = imageHeight * 1.0 / componentHeight;

    scale = 1 / Math.max(wK, hK);
//    System.out.println("Calculated Scale : " + scale);
    return scale;
  }

  // overrides the paint method of Component class
  @Override
  public void paint(Graphics g) {
    recalculatePaintParams();

    Double lx = (-paintX) * 1.0 / paintW * doubleImage.getWidth();
    Double rx = (getWidth() - paintX) * 1.0 / paintW * doubleImage.getWidth();
    Double ly = (-paintY) * 1.0 / paintH * doubleImage.getHeight();
    Double ry = (getHeight() - paintY) * 1.0 / paintH * doubleImage.getHeight();

    if (lx < 0) lx = 0.0;
    if (ly < 0) ly = 0.0;
    if (rx > doubleImage.getWidth()) rx = doubleImage.getWidth() * 1.0;
    if (ry > doubleImage.getHeight()) ry = doubleImage.getHeight() * 1.0;

//    BufferedImage preview  = doubleImage.getBufferedImagePreview(
//          new DoubleImageCropParams(lx.intValue(), ly.intValue(), rx.intValue(), ry.intValue()),
//          getWidth(), getHeight());

//    BufferedImage preview = doubleImage.getBufferedImage();
    BufferedImage preview = doubleImage.getBufferedImageAsync();
    g.drawImage(preview, paintX, paintY, paintW, paintH, this);
    if (afterPaintCallback != null) {
      afterPaintCallback.accept(this);
    }
  }

  // overrides the method in Component class, to determine the window size
  @Override
  public Dimension getPreferredSize() {
    if (doubleImage == null) {
      return new Dimension(100, 100);
    } else {
      // make sure the window is not two small to be seen
      return new Dimension(Math.max(100, doubleImage.getWidth() / 6),
          Math.max(100, doubleImage.getHeight() / 6));
    }
  }
}