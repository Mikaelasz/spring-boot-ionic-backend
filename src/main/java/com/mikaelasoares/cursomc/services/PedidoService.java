package com.mikaelasoares.cursomc.services;

//import java.awt.print.Pageable;
import java.util.Date;
import java.util.Optional;

import com.mikaelasoares.cursomc.domain.*;
import com.mikaelasoares.cursomc.repositories.*;
import com.mikaelasoares.cursomc.security.UserSS;
import com.mikaelasoares.cursomc.services.exceptions.AuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import com.mikaelasoares.cursomc.domain.enums.EstadoPagamento;
import com.mikaelasoares.cursomc.services.exceptions.ObjectNotFoundException;


@Service
public class PedidoService {
	
	@Autowired
	private PedidoRepository repo;
	@Autowired
	private BoletoService boletoService;
	@Autowired
	private PagamentoRepository pagamentoRepository;
	@Autowired
	private ProdutoService produtoService;
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	@Autowired
	private ClienteRepository clienteRepository;
	@Autowired
	private ProdutoRepository produtoRepository;
	@Autowired
	private ClienteService clienteService;
	@Autowired
	private EmailService emailService;
	
	public Pedido find(Integer id) {
		Optional<Pedido> obj = repo.findById(id);
		
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + 
										Pedido.class.getName()));	
	}
	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if(obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		for(ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			Optional<Produto> p = Optional.ofNullable(produtoService.find(ip.getProduto().getId()));
			ip.setProduto(p.get());
			ip.setPreco(p.get().getPreco());

			ip.setPedido(obj);
		}
		itemPedidoRepository.saveAll(obj.getItens());
		emailService.sendOrderConfirmationHtmlEmail(obj);
		return obj;
	}

	public Page<Pedido> findPage(Integer page, Integer linesPerPage, String orderBy, String direction){

		UserSS user = UserService.authenticated();
		if(user == null){
			throw new AuthorizationException("Acesso negado");
		}
		Pageable pageRequest = PageRequest.of(page,linesPerPage,Sort.Direction.valueOf(direction), orderBy);
		Cliente cliente =  clienteService.find(user.getId());
		return repo.findByCliente(cliente, pageRequest);

	}

}
