/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.adaptiveenergy.imagej;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.text.NumberFormatter;

import com.ArtLogic.AArtLogic;
import com.ArtLogic.Help.AHelpDialog;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class Wallis_Filter implements PlugInFilter {
	protected ImagePlus image;

	// image property members
	private int width;
	private int height;

	// plugin parameters
	public double value;
	public String name;

	@Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}

		image = imp;
		return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	}

	@Override
	public void run(ImageProcessor ip) {
		// get width and height
		width = ip.getWidth();
		height = ip.getHeight();
		new WallisWindow(image,ip);
	}

	public void showAbout() {
		IJ.showMessage("Wallis Filter");
	}

}

class WallisWindow extends JFrame{
	private JFormattedTextField textField_WindowSize;
	private JFormattedTextField textField_DesiredMean;
	private JFormattedTextField textField_DesiredStdev;
	
	NumberFormatter MeanFieldFormatter;
	NumberFormatter StdevFieldFormatter;
	NumberFormatter WindowSizeFieldFormatter;
	
	private int width;
	private int height;
	
	ImagePlus imp;
	private ImageProcessor ip;
	ImageStatistics measure;
	
	

	WallisWindow(ImagePlus imp, ImageProcessor ip) {
		this.ip = ip;
		this.imp = imp;
		width = ip.getWidth();
		height = ip.getHeight();
		measure = ip.getStatistics();
		
		NumberFormat MeanFieldFormat = new DecimalFormat("#0.00"); 
		MeanFieldFormatter = new NumberFormatter(MeanFieldFormat);
		
		NumberFormat StdevFieldFormat = new DecimalFormat("#0.00"); 
		StdevFieldFormatter = new NumberFormatter(StdevFieldFormat);
		
		NumberFormat WindowSizeFieldFormat = NumberFormat.getIntegerInstance();
		WindowSizeFieldFormatter = new NumberFormatter(WindowSizeFieldFormat);
		WindowSizeFieldFormatter.setMinimum(0);
		WindowSizeFieldFormatter.setMaximum(Math.min(width,height));
		
		setTitle("Wallis Filter");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 297, 194);		
		JLabel lblWindowSize = new JLabel("Window Size");
		lblWindowSize.setBackground(new Color(240, 240, 240));		
		textField_WindowSize = new JFormattedTextField(WindowSizeFieldFormatter);
		textField_WindowSize.setColumns(10);		
		JLabel lblDesiredMean = new JLabel("Desired Mean");		
		textField_DesiredMean = new JFormattedTextField(MeanFieldFormatter);
		textField_DesiredMean.setColumns(10);		
		JLabel lblDesiredStdev = new JLabel("Desired Stdev");		
		textField_DesiredStdev = new JFormattedTextField(StdevFieldFormatter);
		textField_DesiredStdev.setColumns(10);		
		JButton button = new JButton("?");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AArtLogic.sHelpDialog.showWithSetModalityAndAnchor(false,
			            AHelpDialog.kContrastSensitivity);//to be changed later
			}
		});
		
		JButton btnApply = new JButton("Apply");
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if((textField_WindowSize.getText() != null)&&(textField_DesiredMean.getText()!=null)&&(textField_DesiredStdev.getText()!=null)) {
					int WindowSize = Integer.valueOf(textField_WindowSize.getText());
					double DesiredMean = Double.valueOf(textField_DesiredMean.getText());
					double DesiredStdev = Double.valueOf(textField_DesiredStdev.getText());
					WallisFilter(WindowSize,DesiredMean,DesiredStdev);	
				}
			}
		});
		
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(lblDesiredStdev, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblDesiredMean, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblWindowSize, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
							.addGap(18)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(textField_WindowSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(textField_DesiredMean, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(textField_DesiredStdev, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(84)
							.addComponent(btnApply)
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(button)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblWindowSize)
						.addComponent(textField_WindowSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblDesiredMean)
						.addComponent(textField_DesiredMean, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblDesiredStdev)
						.addComponent(textField_DesiredStdev, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnApply)
						.addComponent(button))
					.addContainerGap(33, Short.MAX_VALUE))
		);
		getContentPane().setLayout(groupLayout);
		setVisible(true);
	}

	protected void WallisFilter(int windowSize, double desiredMean, double desiredStdev) {
		boolean hasRoi = imp.getRoi()!=null;
		if (hasRoi) {
			Roi roi = imp.getRoi();
			Rectangle rect = roi.getBounds();		
			if (Math.min(rect.width,rect.height)<=windowSize) {
				ImagePlus roiImp = imp.duplicate();
				ImageProcessor roiIp = roiImp.getProcessor();
				ImageStatistics measure = roiIp.getStatistics();
				for(int i=0; i<rect.width; i++){
					for(int j=0; j<rect.height; j++){
						int x = rect.x+i;
						int y = rect.y+j;
						ip.putPixelValue(x,y,((ip.get(x,y)-measure.mean)*desiredStdev/measure.stdDev + desiredMean));
					}
				}
			}
			else {
				Roi selection = roi;
				processsplit(hasRoi,rect.width,rect.height,windowSize,desiredMean,desiredStdev);	
				imp.setRoi(selection);
			}
		}
		else {
			processsplit(hasRoi,width,height,windowSize,desiredMean,desiredStdev);		
			Roi roi = null;
			imp.setRoi(roi);
		}	
	}

	private void processsplit(boolean hasRoi, int width2, int height2, int windowSize, double desiredMean, double desiredStdev) {
		int horiz;
		int verti = 0;
		int xstart = 0;
		int ystart =0;
		if(hasRoi) {
			Rectangle rect = imp.getRoi().getBounds();
			xstart = rect.x;
			ystart = rect.y;
		}
		for(horiz=0;(horiz+1)*windowSize<=width2;horiz++) {
			for(verti=0;(verti+1)*windowSize<=height2;verti++) {
				Rectangle splitrect = new Rectangle(xstart+horiz*windowSize, ystart+verti*windowSize, windowSize, windowSize);
				imp.setRoi(splitrect);
				ImagePlus roiImp = imp.duplicate();
				ImageProcessor roiIp = roiImp.getProcessor();
				ImageStatistics measure = roiIp.getStatistics();
				for(int i=0;i<windowSize;i++) {
					for(int j=0;j<windowSize;j++) {
						int x = horiz*windowSize+i+xstart;
						int y = verti*windowSize+j+ystart;					
						ip.putPixelValue(x, y, ((ip.get(x,y)-measure.mean)*desiredStdev/measure.stdDev + desiredMean));
					}
				}
			}
		}
		//for the rest
		for(int xindex=0; (xindex+1)*windowSize<=width2; xindex++){
			Rectangle splitrect = new Rectangle(xindex*windowSize+xstart, verti*windowSize+ystart, windowSize, height2-verti*windowSize);
			imp.setRoi(splitrect);
			ImagePlus roiImp = imp.duplicate();
			ImageProcessor roiIp = roiImp.getProcessor();
			ImageStatistics measure = roiIp.getStatistics();
			for(int i=0;i<windowSize;i++) {
				for(int j=verti*windowSize+ystart;j<height2;j++) {
					int x = xindex*windowSize+i+xstart;
					int y = j;
					ip.putPixelValue(x, y, ((ip.get(x,y)-measure.mean)*desiredStdev/measure.stdDev + desiredMean));
				}
			}
		}
		
		for(int yindex=0; (yindex+1)*windowSize<=height2; yindex++) {
			Rectangle splitrect = new Rectangle(horiz*windowSize+xstart, yindex*windowSize+ystart, width2-horiz*windowSize, windowSize);
			imp.setRoi(splitrect);
			ImagePlus roiImp = imp.duplicate();
			ImageProcessor roiIp = roiImp.getProcessor();
			ImageStatistics measure = roiIp.getStatistics();
			for(int i=horiz*windowSize+xstart;i<width2;i++) {
				for(int j=0;j<windowSize;j++) {
					int x = i;
					int y = yindex*windowSize+j+ystart;
					ip.putPixelValue(x, y, ((ip.get(x,y)-measure.mean)*desiredStdev/measure.stdDev + desiredMean));
				}
			}
		}
		//for the rest
		Rectangle splitrect = new Rectangle(horiz*windowSize+xstart, verti*windowSize+ystart, width2-horiz*windowSize, height2-verti*windowSize);
		imp.setRoi(splitrect);
		ImagePlus roiImp = imp.duplicate();
		ImageProcessor roiIp = roiImp.getProcessor();
		ImageStatistics measure = roiIp.getStatistics();
		for(int i=horiz*windowSize+xstart;i<width2;i++) {
			for(int j=verti*windowSize+ystart;j<height2;j++) {
				int x = i;
				int y = j;
				ip.putPixelValue(x, y, ((ip.get(x,y)-measure.mean)*desiredStdev/measure.stdDev + desiredMean));
			}
		}		
	}
}