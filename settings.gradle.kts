rootProject.name = "AliucordPlugins"

include(":HelloWorld")
project(":HelloWorld").projectDir = File("./HelloWorld")

include(":GoogleIt")
project(":GoogleIt").projectDir = File("./GoogleIt")