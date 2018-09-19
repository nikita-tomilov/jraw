package com.programmer74.jrawtool;

import com.programmer74.jrawtool.components.DisplayingSlider;
import com.programmer74.jrawtool.components.HistogramComponent;
import com.programmer74.jrawtool.converters.JpegImage;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.components.DoubleImageComponent;
import com.programmer74.jrawtool.converters.PGMImage;
import com.programmer74.jrawtool.doubleimage.DoubleImageDefaultValues;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main{

  //./dcraw -4 -D -v -c DSC_1801.NEF > file
  public static void main(String[] args) {

    String filename = args[0];

    DoubleImage doubleImage;
    if (filename.toLowerCase().endsWith(".jpg")) {
      doubleImage = JpegImage.loadPicture(filename);
    } else {
      doubleImage = PGMImage.loadPicture(filename);
    }
    DoubleImageComponent doubleImageComponent = new DoubleImageComponent(doubleImage);
    HistogramComponent histogramComponent = new HistogramComponent(doubleImage);
    DoubleImageDefaultValues defaults = doubleImage.getDefaultValues();

    JFrame f = new JFrame("Image: " + filename);
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    f.add(doubleImageComponent);
    f.pack();
    f.setVisible(true);

    JFrame f2 = new JFrame("Settings");
    f2.setLayout(new FlowLayout());

    DisplayingSlider redsSlider = new DisplayingSlider("Reds", 0.0, 3.0, defaults.getrK());
    DisplayingSlider greensSlider = new DisplayingSlider("Greens", 0.0, 3.0, defaults.getgK());
    DisplayingSlider bluesSlider = new DisplayingSlider("Blue", 0.0, 3.0, defaults.getbK());


    DisplayingSlider gammaSlider = new DisplayingSlider("Gamma", 0.0, 3.0, defaults.getGamma());
    DisplayingSlider exposureSlider = new DisplayingSlider("Exp", -2.0, 2.0, defaults.getExposure());
    DisplayingSlider brightnessSlider = new DisplayingSlider("Bri", -1.0, 1.0, defaults.getBrigthness());
    DisplayingSlider contrastSlider = new DisplayingSlider("Con", 0.0, 2.0, defaults.getContrast());


    ChangeListener sliderChangeListeners = new ChangeListener() {
      @Override public void stateChanged(final ChangeEvent changeEvent) {
        double rk = redsSlider.getValue();
        double gk = greensSlider.getValue();
        double bk = bluesSlider.getValue();

        double gamma = gammaSlider.getValue();
        double exp = exposureSlider.getValue();
        double bri = brightnessSlider.getValue();
        double con = contrastSlider.getValue();

        doubleImage.setWhiteBalance(rk, gk, bk);
        doubleImage.setGamma(gamma);
        doubleImage.setExposureStop(exp);
        doubleImage.setBrightness(bri);
        doubleImage.setContrast(con);
        doubleImageComponent.repaint();
      }
    };

    redsSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(redsSlider);

    greensSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(greensSlider);

    bluesSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(bluesSlider);

    gammaSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(gammaSlider);

    exposureSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(exposureSlider);

    brightnessSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(brightnessSlider);

    contrastSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(contrastSlider);

    f2.setSize(320, 480);
    f2.setVisible(true);

    doubleImageComponent.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == 2) {
          //this is middleclick handler
          int onImageX = doubleImageComponent.getOnImageX(e.getX());
          int onImageY = doubleImageComponent.getOnImageY(e.getY());
          System.out.println("onImageCursor at " + onImageX + " : " + onImageY);

          double[] pixel = doubleImage.getPixels()[onImageX][onImageY];
          double r = pixel[0];
          double g = pixel[1];
          double b = pixel[2];
          double max = Math.max(r, Math.max(g, b));
          double rk = max / r;
          double gk = max / g;
          double bk = max / b;

          redsSlider.setValue(rk);
          greensSlider.setValue(gk);
          bluesSlider.setValue(bk);
        }
      }
    });

    doubleImageComponent.setAfterPaintCallback(e -> {
      //System.out.println("I was painted");
    });

    JFrame f3 = new JFrame("Histogram");
    f3.add(histogramComponent);
    f3.setSize(256, 512);
    f3.setVisible(true);
  }
}