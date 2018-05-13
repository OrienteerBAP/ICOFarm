package org.orienteer.model;

import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;
import java.util.Objects;

public class TransferEvent {
    private final Transaction transaction;
    private final EthBlock.Block block;
    private final BigInteger tokens;


    public TransferEvent(Transaction transaction, EthBlock.Block block, BigInteger tokens) {
        this.transaction = transaction;
        this.block = block;
        this.tokens = tokens;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public EthBlock.Block getBlock() {
        return block;
    }

    public BigInteger getTokens() {
        return tokens;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferEvent that = (TransferEvent) o;
        return Objects.equals(transaction, that.transaction) &&
                Objects.equals(block, that.block) &&
                Objects.equals(tokens, that.tokens);
    }

    @Override
    public int hashCode() {

        return Objects.hash(transaction, block, tokens);
    }

    @Override
    public String toString() {
        return "TransferEvent{" +
                "transaction=" + transaction.getHash() +
                ", block=" + block.getNumber().toString() +
                ", tokens=" + tokens +
                '}';
    }
}
