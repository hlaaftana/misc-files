package ah

import groovy.transform.CompileStatic

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage
import java.text.SimpleDateFormat

@CompileStatic
class MainTest {
	static void main(String[] args) throws IOException {
		long a = System.currentTimeMillis()
		KismetTest2.getX()
		System.out.println("template and let took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		KismetTest2.buildA()
		System.out.println("template and let took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		KismetTest2.test()
		System.out.println("template and let took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		KismetTest.getX()
		System.out.println("Kismet took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		KismetTest.buildA()
		System.out.println("Kismet took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		KismetTest.test()
		System.out.println("Kismet took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		OldKismetTest.getX()
		System.out.println("Old Kismet took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		OldKismetTest.buildA()
		System.out.println("Old Kismet took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		OldKismetTest.test()
		System.out.println("Old Kismet took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		DynKismetTest.getX()
		System.out.println("ah.Dynamic Kismet took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		DynKismetTest.buildA()
		System.out.println("ah.Dynamic Kismet took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		DynKismetTest.test()
		System.out.println("ah.Dynamic Kismet took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		KismetTest3.getX()
		System.out.println("KismetW took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		KismetTest3.buildA()
		System.out.println("KismetW took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		KismetTest3.test()
		System.out.println("KismetW took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		VMKismetTest.buildA()
		System.out.println("VM took " + (System.currentTimeMillis() - a) + "ms")
		a = System.currentTimeMillis()
		VMKismetTest.test()
		System.out.println("VM took " + (System.currentTimeMillis() - a) + "ms")
		int times = 60, width = 30, height = 1500
		int[] java = new int[times]
		for (int i = 0; i < times; ++i) {
			a = System.currentTimeMillis()
			Java.test()
			java[i] = (int) (System.currentTimeMillis() - a)
		}
		int[] statik = new int[times]
		for (int i = 0; i < times; ++i) {
			a = System.currentTimeMillis()
			Static.test()
			statik[i] = (int) (System.currentTimeMillis() - a)
		}
		int[] dynamic = new int[times]
		for (int i = 0; i < times; ++i) {
			a = System.currentTimeMillis()
			Dynamic.test()
			dynamic[i] = (int) (System.currentTimeMillis() - a)
		}
		int[] w = new int[times]
		for (int i = 0; i < times; ++i) {
			a = System.currentTimeMillis()
			KismetTest3.test()
			w[i] = (int) (System.currentTimeMillis() - a)
		}
		int[] kismet = new int[times]
		for (int i = 0; i < times; ++i) {
			a = System.currentTimeMillis()
			KismetTest.test()
			kismet[i] = (int) (System.currentTimeMillis() - a)
		}
		int[] templateAndLet = new int[times]
		for (int i = 0; i < times; ++i) {
			a = System.currentTimeMillis()
			KismetTest2.test()
			templateAndLet[i] = (int) (System.currentTimeMillis() - a)
		}
		int[] oldKismet = new int[times]
		for (int i = 0; i < times; ++i) {
			a = System.currentTimeMillis()
			OldKismetTest.test()
			oldKismet[i] = (int) (System.currentTimeMillis() - a)
		}
		int[] dynamicKismet = new int[times]
		for (int i = 0; i < times; ++i) {
			a = System.currentTimeMillis()
			DynKismetTest.test()
			dynamicKismet[i] = (int) (System.currentTimeMillis() - a)
		}
		int[] vm = new int[times]
		for (int i = 0; i < times; ++i) {
			a = System.currentTimeMillis()
			VMKismetTest.test()
			vm[i] = (int) (System.currentTimeMillis() - a)
		}
		BufferedImage image = new BufferedImage(width * times, height, BufferedImage.TYPE_INT_RGB)
		Graphics2D graphics = image.createGraphics()
		graphics.setColor(Color.WHITE)
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight())
		graphics.setStroke(new BasicStroke(5))
		graphics.setColor(Color.RED)
		for (int i = 1; i < times; ++i) {
			int f = dynamic[i - 1], s = dynamic[i]
			graphics.drawLine((i - 1) * width, f, i * width, s)
		}
		graphics.setColor(Color.ORANGE)
		for (int i = 1; i < times; ++i) {
			int f = statik[i - 1], s = statik[i]
			graphics.drawLine((i - 1) * width, f, i * width, s)
		}
		graphics.setColor(Color.BLUE)
		for (int i = 1; i < times; ++i) {
			int f = java[i - 1], s = java[i]
			graphics.drawLine((i - 1) * width, f, i * width, s)
		}
		graphics.setColor(Color.BLACK)
		for (int i = 1; i < times; ++i) {
			int f = oldKismet[i - 1], s = oldKismet[i]
			graphics.drawLine((i - 1) * width, f, i * width, s)
		}
		graphics.setColor(Color.GREEN)
		for (int i = 1; i < times; ++i) {
			int f = templateAndLet[i - 1], s = templateAndLet[i]
			graphics.drawLine((i - 1) * width, f, i * width, s)
		}
		graphics.setColor(Color.MAGENTA)
		for (int i = 1; i < times; ++i) {
			int f = dynamicKismet[i - 1], s = dynamicKismet[i]
			graphics.drawLine((i - 1) * width, f, i * width, s)
		}
		graphics.setColor(Color.PINK)
		for (int i = 1; i < times; ++i) {
			int f = kismet[i - 1], s = kismet[i]
			graphics.drawLine((i - 1) * width, f, i * width, s)
		}
		graphics.setColor(new Color(0xb707a7))
		for (int i = 1; i < times; ++i) {
			int f = w[i - 1], s = w[i]
			graphics.drawLine((i - 1) * width, f, i * width, s)
		}
		graphics.setColor(new Color(0x0f99a0))
		for (int i = 1; i < times; ++i) {
			int f = vm[i - 1], s = vm[i]
			graphics.drawLine((i - 1) * width, f, i * width, s)
		}
		graphics.dispose()
		ImageIO.write(image, "png", new File("kismets benchmark at " +
				new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(new Date()) + ".png"))
	}
}
