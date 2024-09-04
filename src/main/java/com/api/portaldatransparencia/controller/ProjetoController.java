import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/projetos")
public class ProjetoController {

	@Autowired
	private ProjetoService projetoService;

	@PostMapping
	public Projeto criarProjeto(@RequestBody Projeto projeto) {
		return projetoService.salvarProjeto(projeto);
	}

	@GetMapping
	public List<Projeto> listarProjetos() {
		return projetoService.listarProjetos();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Projeto> buscarProjetoPorId(@PathVariable Long id) {
		Optional<Projeto> projeto = projetoService.buscarProjetoPorId(id);
		return projeto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarProjeto(@PathVariable Long id) {
		projetoService.deletarProjeto(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}")
	public ResponseEntity<Projeto> atualizarProjeto(@PathVariable Long id, @RequestBody Projeto projeto) {
		Optional<Projeto> projetoExistente = projetoService.buscarProjetoPorId(id);
		if (projetoExistente.isPresent()) {
			projeto.setId(id);
			return ResponseEntity.ok(projetoService.salvarProjeto(projeto));
		}
		return ResponseEntity.notFound().build();
	}
}
