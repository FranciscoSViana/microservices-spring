package br.com.fsv.microservice.transportador.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fsv.microservice.transportador.dto.EntregaDTO;
import br.com.fsv.microservice.transportador.dto.VoucherDTO;
import br.com.fsv.microservice.transportador.model.Entrega;
import br.com.fsv.microservice.transportador.repository.EntregaRepository;

@Service
public class EntregaService {

	@Autowired
	private EntregaRepository entregaRepository;
	
	public VoucherDTO reservaEntrega(EntregaDTO pedidoDTO) {
		
		Entrega entrega = new Entrega();
		entrega.setDataParaBusca(pedidoDTO.getDataParaEntrega());
		entrega.setPrevisaoParaEntrega(pedidoDTO.getDataParaEntrega().plusDays(11));
		entrega.setEnderecoDestino(pedidoDTO.getEnderecoDestino());
		entrega.setEnderecoOrigem(pedidoDTO.getEnderecoOrigem());
		entrega.setPedidoId(pedidoDTO.getPedidoId());
		
		entregaRepository.save(entrega);
		
		VoucherDTO voucher = new VoucherDTO();
		voucher.setNumero(entrega.getId());
		voucher.setPrevisaoParaEntrega(entrega.getPrevisaoParaEntrega());
		
		return voucher;
	}
}
