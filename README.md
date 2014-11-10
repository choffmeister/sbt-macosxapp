# sbt-macosxapp

## Usage

Add the following line to your `project/plugins.sbt` file:

~~~ scala
addSbtPlugin("de.choffmeister" % "sbt-macosxapp" % "0.0.1")
~~~

Add the following lines to you `build.sbt` file:

~~~ scala
import de.choffmeister.sbt.MacOSXAppPlugin._

macosxAppSettings

macosxAppName := "My Application"

macosxAppMainClass := "com.domain.Application"

macosxAppIcon := Some(baseDirectory.value / "src/main/resources/icon.icns")

macosxAppJavaJVMOptions := Seq(
  "-Dapple.laf.useScreenMenuBar=true",
  "-Dapple.awt.UIElement=true"
)

// you might want to use other plugins like sbt-pack to get a list of all needed jars
macosxAppJavaJars := Seq(/* ... */)
~~~

Now you can run to generate the app package at `target/My Application.app`:

~~~ bash
$ sbt macosxAppPackage
~~~

## License

Published under the permissive [MIT](http://opensource.org/licenses/MIT) license.
