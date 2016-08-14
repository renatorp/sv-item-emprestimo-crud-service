package br.pucminas.prod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import br.pucminas.prod.entity.Cliente;
import br.pucminas.prod.entity.ItemEmprestimo;

@SpringBootApplication
@RequestMapping(value = "/itens")
public class ItemEmprestimoCrudServiceApplication {

	private static int nextId = 1;
	private static Map<Integer, ItemEmprestimo> poolItens = new HashMap<>();

	public static void main(String[] args) {
		SpringApplication.run(ItemEmprestimoCrudServiceApplication.class, args);
	}

	@RequestMapping("/")
	@ResponseBody
	public List<ItemEmprestimo> pesquisar(ItemEmprestimo item) {
		Stream<ItemEmprestimo> stream = poolItens.values().stream();
		Stream<ItemEmprestimo> filter = stream
				.filter(new Predicate<ItemEmprestimo>() {

					@Override
					public boolean test(ItemEmprestimo t) {
						return filterItem(t, item);
					}

				});
		List<ItemEmprestimo> collect = filter.collect(Collectors.toList());
		return collect;
	}

	private boolean filterItem(ItemEmprestimo item, ItemEmprestimo filter) {
		if (!StringUtils.isEmpty(filter.getAutor())
				&& !item.getAutor().contains(filter.getAutor())) {
			return false;
		}
		if (!StringUtils.isEmpty(filter.getEdicao())
				&& !item.getEdicao().contains(filter.getEdicao())) {
			return false;
		}
		if (!StringUtils.isEmpty(filter.getTipo())
				&& !item.getTipo().contains(filter.getTipo())) {
			return false;
		}
		if (!StringUtils.isEmpty(filter.getTitulo())
				&& !item.getTitulo().contains(filter.getTitulo())) {
			return false;
		}
		return true;
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	@ResponseBody
	public ItemEmprestimo criar(@RequestBody ItemEmprestimo item) {
		
		RestTemplate restTemplate = new RestTemplate();
	    Cliente proprietario = restTemplate.getForObject("http://localhost:8080/clientes/{id}", Cliente.class, item.getProprietario().getId());
	    
	    if (proprietario != null) {
	    	item.setProprietario(proprietario);
	    	item.setDisponivel(true);
	    	item.setId(nextId++);
	    	poolItens.put(item.getId(), item);
	    	return item;
	    }
	    return null;
	}

	@RequestMapping(value = "/", method = RequestMethod.PUT)
	@ResponseBody
	public ItemEmprestimo atualizar(@RequestBody ItemEmprestimo item) {
		ItemEmprestimo persistedItem = poolItens.get(item.getId());
		BeanUtils.copyProperties(item, persistedItem);
		return item;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ItemEmprestimo remover(@PathVariable Integer id) {
		return poolItens.remove(id);
	}
	

	@RequestMapping("/{id}")
	@ResponseBody
	public ItemEmprestimo findById(@PathVariable Integer id) {
		return poolItens.get(id);
	}
	
}
