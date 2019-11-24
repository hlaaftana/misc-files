package hlaaftana.pink

import groovy.transform.CompileStatic
import javafx.scene.image.Image
import javafx.stage.Stage

@CompileStatic
@Singleton(property = 'INSTANCE')
class Game {
	Stage stage

	void init(Stage stage) {
		this.stage = stage
	}

	Image loadResource(String name) {
		new Image('/resources/' + name)
	}
}
