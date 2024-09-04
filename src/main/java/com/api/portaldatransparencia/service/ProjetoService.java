@Service
public class ProjetoService {

	@Autowired
	private ProjetoRepository projetoRepository;

	public Projeto salvarProjeto(Projeto projeto) {
		return projetoRepository.save(projeto);
	}

	public List<Projeto> listarProjetos() {
		return projetoRepository.findAll();
	}

	public Optional<Projeto> buscarProjetoPorId(Long id) {
		return projetoRepository.findById(id);
	}

	public void deletarProjeto(Long id) {
		projetoRepository.deleteById(id);
	}
}
