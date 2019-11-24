package hlaaftana.pink

import groovy.transform.CompileStatic
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox

@CompileStatic
class MainMenu extends VBox {
	final Image title = Game.INSTANCE.loadResource('title.png')

	MainMenu() {
		setAlignment(Pos.TOP_CENTER)
		children.add(new ImageView(title))
	}
}
