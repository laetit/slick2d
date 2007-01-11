package org.newdawn.slick.tools.hiero;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * TODO: Document this class
 *
 * @author kevin
 */
public class FontTextureGenerator {
	private BufferedImage image;
	private BufferedImage overlay;
	private DataSet data;
	private Font font;
	private int width;
	private int height;
	private CharSet set;
	
	public BufferedImage getImage() {
		return image;
	}
    
	public BufferedImage getOverlay() {
		return overlay;
	}
	
	public DataSet getDataSet() {
		return data;
	}
	
    public void generate(Font font, int width, int height, CharSet set) {
        int xp = 0;
        int yp = 0;
        
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0f,0f,0f,0f));
        g.fillRect(0,0,width,height);
        overlay = new BufferedImage(width+1, height+1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D og = (Graphics2D) overlay.getGraphics();

        int xpadding = 0;
        int ypadding = 0;
        
        g.setFont(font);
        og.setColor(Color.red);
        og.drawRect(0,0,width,height);
        
        int des = g.getFontMetrics().getMaxDescent();
        int lineHeight = des + g.getFontMetrics().getMaxAscent() + ypadding;
        yp += lineHeight-des;

        data = new DataSet(font.getName(), font.getSize(), lineHeight, width, height, set.getName(), "font.png");
        
        ArrayList rects = new ArrayList();
        
        for (int i=set.getStart();i<=set.getEnd();i++) {    
            g.setColor(Color.white);       
            char first = (char) i;
            
            int xoffset = 0;
            int lsb = getGlyphLSB(g, first);
            int rsb = getGlyphRSB(g, first);
            int fontWidth = getGlyphAdvanceX(g, first) + (xpadding * 2);
            int fontHeight = getGlyphHeight(g, first)+1;
            int yoffset = getGlyphYOffset(g, first)-1;
            int advance = fontWidth - 1;
            
            if (lsb < 0) {
            	xoffset = -lsb + 1;
            	fontWidth += xoffset;
            }
            if (rsb < 0) {
            	fontWidth -= rsb - 1;
            }
            
            if (xp + fontWidth >= width) {
                xp = 0;
                yp += lineHeight;
            }
            
            GlyphRect rect = new GlyphRect();
            rect.c = first;
            rect.x = xp;
            rect.y = yp+yoffset;
            rect.xDrawOffset = xoffset + xpadding;
            rect.yDrawOffset = -1 - ypadding;
            rect.width = fontWidth;
            rect.height = fontHeight;
            rect.yoffset = yoffset;
            rect.advance = advance;
            
            rects.add(rect);
            xp += fontWidth;
        }
        
        xp = 0;
        yp = 0;
        
        Collections.sort(rects, new Comparator() {

			public int compare(Object a, Object b) {
				GlyphRect first = (GlyphRect) a;
				GlyphRect second = (GlyphRect) b;
				
				return second.height - first.height;
			}
        	
        });
        
        int stripHeight = -1;
        int stripY = 0;

        for (int i=0;i<rects.size();i++) {
        	GlyphRect rect = (GlyphRect) rects.get(i);

        	if (xp+rect.width > width) {
        		xp = 0;
        		stripY += stripHeight + 1;
        		stripHeight = -1;
        	}
        	
        	if (stripHeight == -1) {
        		stripHeight = rect.height;
        	}
        	
        	rect.x = xp;
        	rect.y = stripY;
        	
        	rect.drawGlyph(g);
        	rect.drawOverlay(og);
        
        	xp += rect.width + 1;
        }


        Collections.sort(rects, new Comparator() {

			public int compare(Object a, Object b) {
				GlyphRect first = (GlyphRect) a;
				GlyphRect second = (GlyphRect) b;
				
				return first.c - second.c;
			}
        	
        });
        
        for (int i=0;i<rects.size();i++) {
        	GlyphRect rect = (GlyphRect) rects.get(i);
        	rect.storeData(g, data, set);
        }
    }

    private int getKerning(Graphics2D g, char first, char second) {
    	String text = first+""+second;
        GlyphVector vector = g.getFont().layoutGlyphVector(g.getFontRenderContext(), text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);

        Shape shape2 = vector.getGlyphPixelBounds(1, g.getFontRenderContext(), 0, 0);
        return (int) (shape2.getBounds().x - vector.getGlyphMetrics(0).getAdvanceX());
    }

    private int getGlyphYOffset(Graphics2D g, char c) {
        String text = ""+c;
        GlyphVector vector = g.getFont().layoutGlyphVector(g.getFontRenderContext(), text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);

        Rectangle bounds = vector.getPixelBounds(g.getFontRenderContext(), 0,0);
        
        return (int) (bounds.y);
    }
    
    private int getGlyphHeight(Graphics2D g, char c) {
        String text = ""+c;
        GlyphVector vector = g.getFont().layoutGlyphVector(g.getFontRenderContext(), text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);

        return (int) vector.getGlyphVisualBounds(0).getBounds().height;
    }
    
    private int getGlyphAdvanceX(Graphics2D g, char c) {
        String text = ""+c;
        GlyphVector vector = g.getFont().layoutGlyphVector(g.getFontRenderContext(), text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);

        return (int) vector.getGlyphMetrics(0).getAdvanceX();
    }

    private int getGlyphAdvanceY(Graphics2D g, char c) {
        String text = ""+c;
        GlyphVector vector = g.getFont().layoutGlyphVector(g.getFontRenderContext(), text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);

        return (int) vector.getGlyphMetrics(0).getAdvanceY();
    }
    
    private int getGlyphLSB(Graphics2D g, char c) {
        String text = ""+c;
        GlyphVector vector = g.getFont().layoutGlyphVector(g.getFontRenderContext(), text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);

        return (int) vector.getGlyphMetrics(0).getLSB();
    }

    private int getGlyphRSB(Graphics2D g, char c) {
        String text = ""+c;
        GlyphVector vector = g.getFont().layoutGlyphVector(g.getFontRenderContext(), text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);

        return (int) vector.getGlyphMetrics(0).getRSB();
    }
    
    private class GlyphRect {
    	public char c;
    	public int x;
    	public int y;
    	public int width;
    	public int height;
    	public int advance;
    	public int yoffset;
    	public int xDrawOffset;
    	public int yDrawOffset;
    	
    	public void storeData(Graphics2D g, DataSet data, CharSet set) {
            data.addCharacter(c, advance, x,y, width, height,yoffset);
            for (int j=set.getStart();j<=set.getEnd();j++) {    
            	char second = (char) j;
            	
            	int kerning = getKerning(g, c, second);
            	if (kerning != 0) {
            		data.addKerning(c, second, kerning);
            	}
            }
    	}
    	
    	public void drawGlyph(Graphics2D g) {
    		g.setColor(Color.white);
    		g.drawString(""+c, x + xDrawOffset, y - yoffset + yDrawOffset);
    	}
    	
    	public void drawOverlay(Graphics2D og) {
    		og.setColor(Color.yellow);
            og.drawRect(x,y,width,height);
    	}
    }
}