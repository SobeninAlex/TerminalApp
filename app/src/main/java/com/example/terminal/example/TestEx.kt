package com.example.terminal.example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun TestEx() {

    // если объект не примитивный, и не реализует Parcelable, то с rememberSaveable возникнут проблемы при смене конфигурации
    // под капотом rememberSaveable использует Bundle
    // есть 3 способа как сохранить объект в Bundle при помощи rememberSaveable

    var testData by rememberSaveable(saver = TestData.Saver) {
        mutableStateOf(TestData(number = 0, text = "some text"))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { testData = testData.copy(number = testData.number + 1) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Text: $testData"
        )
    }
}


//// 1 способ, просто реализовать интерфейс Parcelable
//@Parcelize
//data class TestData(
//    val number: Int
//) : Parcelable


//// 2 способ -> если для создания объекта требуется только один параметр, в данном случае это number: Int
//data class TestData(
//    val number: Int
//) {
//    companion object {
//        //создали объект Saver, который говорит о том, как можно сохранить, и восстановить объект MutableState<TestData>
//        val Saver: Saver<MutableState<TestData>, Int> = Saver(
//            save = {
//                it.value.number
//            },
//            restore = {
//                mutableStateOf(TestData(number = it))
//            }
//        )
//    }
//}


// 3 способ -> если для создания объекта требуется несколько параметров
data class TestData(
    val number: Int,
    val text: String
) {
    companion object {
        //создали объект Saver, который говорит о том, как можно сохранить, и восстановить объект MutableState<TestData>
        val Saver: Saver<MutableState<TestData>, Any> = listSaver(
            save = {
                val testData = it.value
                listOf(testData.number, testData.text)
            },
            restore = {
                val testData = TestData(
                    number = it.first() as Int,
                    text = it.last() as String
                )
                mutableStateOf(testData)
            }
        )
    }
}
