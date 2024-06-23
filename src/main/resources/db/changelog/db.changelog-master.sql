-- liquibase formatted sql

--changeset kotkoms:create-table-wallet
CREATE TABLE public.wallet (
    wallet_id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    balance BIGINT
);
