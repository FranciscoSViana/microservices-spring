package br.com.fsv.microservice.loja.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import br.com.fsv.microservice.loja.client.FornecedorClient;
import br.com.fsv.microservice.loja.client.TransportadorClient;
import br.com.fsv.microservice.loja.controller.dto.CompraDTO;
import br.com.fsv.microservice.loja.controller.dto.InfoEntregaDTO;
import br.com.fsv.microservice.loja.controller.dto.InfoFornecedorDTO;
import br.com.fsv.microservice.loja.controller.dto.InfoPedidoDTO;
import br.com.fsv.microservice.loja.controller.dto.VoucherDTO;
import br.com.fsv.microservice.loja.model.Compra;
import br.com.fsv.microservice.loja.model.CompraState;
import br.com.fsv.microservice.loja.repository.CompraRepository;

@Service
public class CompraService {
	
	@Autowired
	private FornecedorClient fornecedorClient;
	
	@Autowired
	private TransportadorClient transportadorClient;
	
	@Autowired
	private CompraRepository compraRepository;
	
	@HystrixCommand(threadPoolKey = "getByIdThreadPool")
	public Compra getById(Long id) {
		return compraRepository.findById(id).orElse(new Compra());
	}
	
	@HystrixCommand(fallbackMethod = "realizaCompraFallback", threadPoolKey = "realizaCompraThreadPool")
	public Compra realizaCompra(CompraDTO compra) {
		
		Compra compraSalva = new Compra();
		compraSalva.setState(CompraState.RECEBIDO);
		compraSalva.setEnderecoDestino(compra.getEndereco().toString());
		compraRepository.save(compraSalva);
		compra.setCompraId(compraSalva.getId());
		
		InfoFornecedorDTO info = fornecedorClient.getInfoPorEstado(compra.getEndereco().getEstado());
		InfoPedidoDTO pedido = fornecedorClient.realizaPedido(compra.getItens());
		compraSalva.setState(CompraState.PEDIDO_REALIZADO);
		
		compraSalva.setPedidoId(pedido.getId());
		compraSalva.setTempoDePreparo(pedido.getTempoDePreparo());
		compraRepository.save(compraSalva);
		
		InfoEntregaDTO entregaDTO = new InfoEntregaDTO();
		entregaDTO.setPedidoId(pedido.getId());
		entregaDTO.setDataParaEntrega(LocalDate.now().plusDays(pedido.getTempoDePreparo()));
		entregaDTO.setEnderecoOrigem(info.getEndereco());
		entregaDTO.setEnderecoDestino(compra.getEndereco().toString());
		VoucherDTO voucher = transportadorClient.reservaEntrega(entregaDTO);
		compraSalva.setState(CompraState.RESERVA_ENTREGA_REALIZADO);
		compraSalva.setDataParaEntrega(voucher.getPrevisaoParaEntrega());
		compraSalva.setVoucher(voucher.getNumero());
		compraRepository.save(compraSalva);
				
		return compraSalva;
	}
	
	public Compra realizaCompraFallback(CompraDTO compra) {
		
		if (compra.getCompraId() != null) {
			return compraRepository.findById(compra.getCompraId()).get();
		}
		
		Compra compraFallback = new Compra();
		compraFallback.setEnderecoDestino(compra.getEndereco().toString());
		
		return compraFallback;
	}
}
