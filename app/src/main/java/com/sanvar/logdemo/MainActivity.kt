package com.sanvar.logdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sanvar.log.KLog
import com.sanvar.log.LogCatPrinter
import com.sanvar.log.SmartFilePrinter
import com.sanvar.log.UDPLogPrinter
import com.sanvar.log.WarpLogPrinter
import com.sanvar.logdemo.ui.theme.KlogDemoTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val warpLogPrinter = WarpLogPrinter()
        warpLogPrinter.addLogPrinter(LogCatPrinter("KLog"))
        warpLogPrinter.addLogPrinter(UDPLogPrinter(9999))

        val dir = File(this.filesDir, "KLog")
        warpLogPrinter.addLogPrinter(SmartFilePrinter(dir.absolutePath))


        KLog.setup(true, output = warpLogPrinter)


        setContent {
            KlogDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .padding(innerPadding)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text("KLog Demo")

                        Button(
                            onClick = { KLog.d { "Log.d" } },
                            modifier = Modifier.padding(8.dp)
                        ) { Text("Log.d") }
                        Button(
                            onClick = { KLog.i { "Log.i" } },
                            modifier = Modifier.padding(8.dp)
                        ) { Text("Log.i") }
                        Button(
                            onClick = { KLog.w { "Log.w" } },
                            modifier = Modifier.padding(8.dp)
                        ) { Text("Log.w") }
                        Button(
                            onClick = { KLog.e { "Log.e" } },
                            modifier = Modifier.padding(8.dp)
                        ) { Text("Log.e") }


                        Button(
                            onClick = { KLog.e(throwable = Exception(" test throwable") ) { "Log.e" } },
                            modifier = Modifier.padding(8.dp)
                        ) { Text("throwable") }

                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KlogDemoTheme {
        Greeting("Android")
    }
}