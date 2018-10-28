import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.List;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Window extends JFrame implements ComponentListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JLabel label;
	private BufferedImage image;

	File imageListFileNew;	

	private List<File> files;
	private File filesNotDeleted[];

	private int imageNumber = 0;

	private boolean fileMustBeChanged = false;

	private final File folder;

	public Window(String fileAlbumName) {
		super("");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(1, 1));

		setExtendedState(MAXIMIZED_BOTH);
		setSize(500, 500);

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Choose folder");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		// chooser.setCurrentDirectory(new File("D:\\a"));

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			folder = chooser.getSelectedFile();
		} else {
			folder = null;

			System.exit(-1);

			return;
		}

		PrintWriter imageListOutput;
	
		imageListFileNew = new File(fileAlbumName);		
		if (!imageListFileNew.exists()) {
			File files[] = folder.listFiles();
			filesNotDeleted = files;

			try {
				imageListFileNew.createNewFile();
				imageListOutput = new PrintWriter(imageListFileNew);
			} catch (Exception e) {
				System.out.println("Error of CFG file creating");
				
				System.exit(-1);

				return;
			}

			for (File imageFile : files) {
				//String text1 = imageFile.getName();
				//String text2 = imageFile.getParent();
				//String text3 = imageFile.getPath();
				//String text5 = text3.replace("/run/media/my/HITACHI/_PHOTO", "");
				//String text4 = text3.replaceAll("_PHOTO", "");
				imageListOutput.println(imageFile.getPath());
			}

			imageListOutput.close();
		}		

		Scanner imageListInput = null;
		files = new ArrayList<File>();

		try {
			imageListInput = new Scanner(new File(fileAlbumName));
		} catch (Exception e) {
			System.out.println("Error - files reading - " + e.getMessage());
			
			System.exit(-1);
			
			return;
		}
		
		while (imageListInput.hasNextLine()) {
			String nextLine = imageListInput.nextLine();

			files.add(new File(nextLine));
		}

		imageListInput.close();

		filesNotDeleted = new File[files.size()];

		int fileNumber = 0;
		for (File tempFile : files) {
			filesNotDeleted[fileNumber] = tempFile;

			fileNumber++;
		}

		if (files.size() > 0) {
			Arrays.sort(files.toArray());

			image = null;

			String fileName = files.get(0).getName();

			if (fileName.length() == 13) {
				if (fileName.charAt(11) == 'f') {
					imageNumber = 1;
					
					setTitle(files.get(1).getName());

					try {
						image = ImageIO.read(files.get(1));
					} catch (Exception e) {
						System.out.println("Error in line 138");
						
						System.exit(-1);

						return;
					}
				}
			}
			if (fileName.length() != 13 || fileName.charAt(11) != 'f') {
				setTitle(files.get(0).getName());
				
				try {
					image = ImageIO.read(files.get(0));
				} catch (Exception e) {
					System.out.println("Error in line 152");
					
					System.exit(-1);

					return;
				}
			}

			setIconImage(image);

			int width = getWidth(), height = getHeight();

			ImageIcon icon = null;

			int scaledWidth;
			try {
				scaledWidth = (int) (image.getWidth() * ((double) height / (double) image.getHeight()));
			} catch (Exception e) {
				System.out.println("Error in line 170");
				
				System.exit(-1);

				return;
			}

			if (scaledWidth <= getWidth())
				icon = new ImageIcon(createResizedCopy(image, scaledWidth, height));
			else
				icon = new ImageIcon(createResizedCopy(image, width,
						(int) (image.getHeight() * ((double) width / (double) image.getWidth()))));

			label = new JLabel(icon);

			add(label);

			addComponentListener(this);
			addKeyListener(this);
		}

		setVisible(true);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (fileMustBeChanged) {
					File imageListFile = new File(fileAlbumName);					
					PrintWriter imageListOutput = null;

					try {
						imageListFile.createNewFile();
						imageListOutput = new PrintWriter(imageListFile);
					} catch (Exception e) {
						System.out.println("Error - cannot create file");
						
						System.exit(-1);

						return;
					}

					for (File imageFile : filesNotDeleted) {
						if (imageFile != null && imageFile.getName() != "")
							imageListOutput.println(imageFile.getPath());
					}

					imageListOutput.close();
				}
			}
		});
	}

	private void changeSize() {
		int width = getWidth(), height = getHeight();

		ImageIcon icon = null;

		int scaledWidth = (int) (image.getWidth() * ((double) height / (double) image.getHeight()));

		if (scaledWidth <= getWidth())
			icon = new ImageIcon(createResizedCopy(image, scaledWidth, height));
		else
			icon = new ImageIcon(createResizedCopy(image, width,
					(int) (image.getHeight() * ((double) width / (double) image.getWidth()))));

		label.setIcon(icon);
	}

	BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight) {
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaledBI.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {

	}

	@Override
	public void componentMoved(ComponentEvent arg0) {

	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		changeSize();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			imageNumber++;
			if (imageNumber == files.size()) {
				imageNumber--;

				return;
			}

			String fileName = files.get(imageNumber).getName();

			if (fileName.length() == 13) {
				if (fileName.charAt(11) == 'f') {
					if (imageNumber < (files.size() - 1))
						imageNumber++;
					else {
						imageNumber--;

						return;
					}
				}
			}
			
			setTitle(files.get(imageNumber).getName());

			try {
				image = ImageIO.read(files.get(imageNumber));
			} catch (IOException e1) {
				System.out.println("Error in line 296");
				
				System.exit(-1);

				return;
			}

			setIconImage(image);

			changeSize();
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			imageNumber--;

			if (imageNumber == -1)
				imageNumber = 0;
			else {
				String fileName = files.get(imageNumber).getName();

				if (fileName.length() == 13) {
					if (fileName.charAt(11) == 'f') {
						if (imageNumber > 0)
							imageNumber--;
						else {
							imageNumber++;

							return;
						}
					}
				}
				
				setTitle(files.get(imageNumber).getName());
				
				try {
					image = ImageIO.read(files.get(imageNumber));
				} catch (IOException e1) {
					System.out.println("Error in line 331");
					
					System.exit(-1);

					return;
				}

				setIconImage(image);

				changeSize();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyChar() == 'd') {
			filesNotDeleted[imageNumber] = null;

			fileMustBeChanged = true;
		} else if (e.getKeyChar() == 'r') {
			if (!imageListFileNew.delete())
				System.out.println("Nie mozna usunac pliku konfiguracyjnego.");

			fileMustBeChanged = false;

			System.exit(-1);

			return;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}
}