CREATE TABLE itens_ordem_servico (
    id BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL,
    peca_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_item_os_ordem_servico FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_os_peca FOREIGN KEY (peca_id) REFERENCES pecas(id)
);

CREATE INDEX idx_itens_os_ordem_servico_id ON itens_ordem_servico(ordem_servico_id);
CREATE INDEX idx_itens_os_peca_id ON itens_ordem_servico(peca_id);
