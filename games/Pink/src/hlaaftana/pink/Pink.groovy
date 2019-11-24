package hlaaftana.pink

import groovy.transform.CompileStatic
import javafx.application.Application
import javafx.stage.Stage

@CompileStatic
class Pink extends Application {
	void start(Stage primaryStage) {
		Game.INSTANCE.init(primaryStage)
	}

	static main(String[] args) {
		launch(args)
	}
}
