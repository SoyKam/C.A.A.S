package com.caas.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.caas.app.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentUserData()

        binding.btnSave.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun loadCurrentUserData() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        
        // Email desde Auth (siempre disponible)
        binding.etEmail.setText(user.email)

        // Resto desde Firestore
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (isAdded && document != null && document.exists()) {
                    binding.etName.setText(document.getString("name"))
                    binding.etPhone.setText(document.getString("phone"))
                    binding.etBusinessName.setText(document.getString("businessName"))
                }
            }
    }

    private fun saveProfileChanges() {
        val uid = auth.currentUser?.uid ?: return
        
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val businessName = binding.etBusinessName.text.toString().trim()

        // Validaciones
        if (name.isEmpty()) {
            binding.tilName.error = "El nombre es obligatorio"
            return
        } else {
            binding.tilName.error = null
        }

        // Preparar datos para Firestore
        val userUpdates = hashMapOf(
            "name" to name,
            "phone" to phone,
            "businessName" to businessName,
            "updatedAt" to System.currentTimeMillis()
        )

        // Guardar usando SetOptions.merge() para no borrar el "role" u otros campos
        db.collection("users").document(uid)
            .set(userUpdates, SetOptions.merge())
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
