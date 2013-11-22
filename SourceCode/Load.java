
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Load {

	String WWW_Root;

	public Load(String WWW_ROOT) {
		// TODO Auto-generated constructor stub
		this.WWW_Root = WWW_ROOT;
	}

	public byte[] load() {

		String filename = "plugin/config.txt";
		// FileInputStream fileStream;

		BufferedReader fileStream;
		String className = "";
		try {
			// reading the name of the plugin
			fileStream = new BufferedReader(new InputStreamReader(
					new FileInputStream(WWW_Root + filename)));
			className = fileStream.readLine();
			Debug.DEBUG(className);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}

		/*String fileToCompile = WWW_Root + "plugin\\" + className + ".java";
		Debug.DEBUG(fileToCompile);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		// compiling the class
		int compilationResult = compiler.run(null, null, null, fileToCompile);
		if (compilationResult == 0) {
			System.out.println("Compilation is successful");
		} else {
			System.out.println("Compilation Failed");
			return null;
		}
*/
		File file = new File(WWW_Root + "plugin/");
		try {
			// Convert File to a URL
			@SuppressWarnings("deprecation")
			URL url = file.toURL();
			URL[] urls = new URL[] { url };

			// Create a new class loader with the directory
			ClassLoader cl = new URLClassLoader(urls);

			// Load in the class; MyClass.class should be located in
			// the directory file:WWW_ROOT\plugin\className
			Class cls = cl.loadClass(className);
			BaseLoad b;
			try {
				b = (BaseLoad) cls.newInstance();
				return b.run();
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;

	}
}
