CREATE TABLE ordens_servico (
    id BIGSERIAL PRIMARY KEY,
    numero_os VARCHAR(50) NOT NULL UNIQUE,
    cliente_id BIGINT NOT NULL,
    equipamento_id BIGINT NOT NULL,
    defeito_relatado TEXT NOT NULL,
    diagnostico_tecnico TEXT,
    status VARCHAR(30) NOT NULL,
    valor_mao_de_obra NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    valor_pecas NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    valor_total NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    data_abertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP,
    observacoes TEXT,
    CONSTRAINT fk_os_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_os_equipamento FOREIGN KEY (equipamento_id) REFERENCES equipamentos(id)
);

CREATE INDEX idx_ordens_servico_numero_os ON ordens_servico(numero_os);
CREATE INDEX idx_ordens_servico_status ON ordens_servico(status);
CREATE INDEX idx_ordens_servico_cliente_id ON ordens_servico(cliente_id);
