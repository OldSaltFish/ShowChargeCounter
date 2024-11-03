package cn.dreamsoul.showchargecounter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.dreamsoul.showchargecounter.ui.theme.ShowChargeCounterTheme


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShowChargeCounterTheme {
                Scaffold(
                    topBar = {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ShowChargeCounter",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                fontSize = 24.sp
                            )
                            IconButton(onClick = {
                                val url = "https://atomgit.com/dreamsoul/ShowChargeCounter"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                                if(intent.resolveActivity(packageManager) != null){
                                startActivity(intent)
//                                }
                            }) {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = "关于",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    BatteryInfoScreen(innerPadding)
                }
            }
        }
    }
}



