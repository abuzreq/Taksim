package examples;


import processing.core.PApplet;

public class GridGeneratorDrawer extends PApplet
{
	public static GridGeneratorDrawer instance;
	boolean drawText = true;
 
	public void settings() 
	{
		instance = this;
		int cellDim = GridGenerator.instance.cellDim;
		int dim = GridGenerator.instance.dim;
		size(cellDim*dim,cellDim*dim );
	}

	public void setup() 
	{
		instance = this;
		int cellDim = GridGenerator.instance.cellDim;
		int dim = GridGenerator.instance.dim;
		surface.setSize(cellDim*dim,cellDim*dim);
	}
		
	public void draw()
	{				
		GridGenerator.instance.draw(this,drawText);
	}
}
