package com.example.secretimages

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secretimages.databinding.ActivityCalculatorBinding
import kotlin.math.floor

class CalculatorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalculatorBinding

    private var operand1: Double? = null
    private var pendingOp: String = ""
    private var startFresh = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        refreshAcLabel()
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        refreshAcLabel()
    }

    private fun refreshAcLabel() {
        binding.btnAC.text = if (PasswordManager.isPasswordSet(this)) "AC" else "SET"
    }

    private fun setupButtons() {
        mapOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
            binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
            binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
            binding.btn9 to "9", binding.btn00 to "00", binding.btnDot to "."
        ).forEach { (btn, d) -> btn.setOnClickListener { appendDigit(d) } }

        binding.btnPlus.setOnClickListener { pressOp("+") }
        binding.btnMinus.setOnClickListener { pressOp("-") }
        binding.btnMultiply.setOnClickListener { pressOp("×") }
        binding.btnDivide.setOnClickListener { pressOp("÷") }
        binding.btnPercent.setOnClickListener { pressPercent() }
        binding.btnBackspace.setOnClickListener { pressBackspace() }
        binding.btnAC.setOnClickListener { pressAcSet() }
        binding.btnEquals.setOnClickListener { pressEquals() }
    }

    private fun readDisplay() = binding.tvDisplay.text.toString()

    private fun setDisplay(s: String) {
        binding.tvDisplay.text = if (s.isEmpty()) "0" else s
    }

    private fun appendDigit(d: String) {
        val cur = readDisplay()
        if (startFresh) {
            setDisplay(if (d == "00") "0" else d)
            startFresh = false
            return
        }
        when {
            cur == "0" && d != "." -> setDisplay(if (d == "00") "0" else d)
            d == "." && cur.contains(".") -> return
            d == "00" && cur == "0" -> return
            else -> setDisplay(cur + d)
        }
    }

    private fun pressOp(op: String) {
        val cur = readDisplay().toDoubleOrNull()
        if (operand1 != null && pendingOp.isNotEmpty() && !startFresh && cur != null) {
            val res = doCalc(operand1!!, pendingOp, cur)
            setDisplay(fmt(res))
            operand1 = res
        } else {
            operand1 = cur
        }
        pendingOp = op
        startFresh = true
    }

    private fun pressPercent() {
        val v = readDisplay().toDoubleOrNull() ?: return
        val res = if (operand1 != null) operand1!! * v / 100.0 else v / 100.0
        setDisplay(fmt(res))
        startFresh = true
    }

    private fun pressBackspace() {
        if (startFresh) return
        val cur = readDisplay()
        setDisplay(if (cur.length <= 1) "0" else cur.dropLast(1))
    }

    private fun pressAcSet() {
        if (!PasswordManager.isPasswordSet(this)) {
            val pwd = readDisplay()
            if (pwd.length >= 4 && pwd.all { it.isDigit() }) {
                PasswordManager.setPassword(this, pwd)
                Toast.makeText(this, "パスワードを設定しました", Toast.LENGTH_SHORT).show()
                clearAll()
                refreshAcLabel()
            } else {
                Toast.makeText(this, "数字4桁以上を入力してください", Toast.LENGTH_SHORT).show()
            }
        } else {
            clearAll()
        }
    }

    private fun pressEquals() {
        val displayed = readDisplay()
        // Unlock check (only when no pending operation)
        if (pendingOp.isEmpty() && PasswordManager.isPasswordSet(this) &&
            PasswordManager.verifyPassword(this, displayed)) {
            startActivity(Intent(this, MainActivity::class.java))
            clearAll()
            return
        }
        // Calculate
        if (operand1 != null && pendingOp.isNotEmpty() && !startFresh) {
            val b = displayed.toDoubleOrNull() ?: return
            val res = doCalc(operand1!!, pendingOp, b)
            setDisplay(fmt(res))
            operand1 = null
            pendingOp = ""
            startFresh = true
        }
    }

    private fun doCalc(a: Double, op: String, b: Double): Double = when (op) {
        "+" -> a + b
        "-" -> a - b
        "×" -> a * b
        "÷" -> if (b != 0.0) a / b else Double.NaN
        else -> b
    }

    private fun clearAll() {
        operand1 = null
        pendingOp = ""
        startFresh = false
        setDisplay("0")
    }

    private fun fmt(v: Double): String = when {
        v.isNaN() || v.isInfinite() -> "エラー"
        v == floor(v) && v >= Long.MIN_VALUE.toDouble() && v <= Long.MAX_VALUE.toDouble() ->
            v.toLong().toString()
        else -> v.toBigDecimal().stripTrailingZeros().toPlainString()
    }
}
