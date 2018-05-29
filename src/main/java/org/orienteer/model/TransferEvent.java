package org.orienteer.model;

import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;
import java.util.Objects;

public class TransferEvent {
    private final Transaction transaction;
    private final EthBlock.Block block;
    private final BigInteger tokens;
    private final String to;

    public TransferEvent(Transaction transaction, EthBlock.Block block, BigInteger tokens, String to) {
        this.transaction = transaction;
        this.block = block;
        this.tokens = tokens;
        this.to = to;
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

    public String getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferEvent that = (TransferEvent) o;
        return Objects.equals(transaction, that.transaction) &&
                Objects.equals(block, that.block) &&
                Objects.equals(tokens, that.tokens) &&
                Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {

        return Objects.hash(transaction, block, tokens, to);
    }

    @Override
    public String toString() {
        return "TransferEvent{" +
                "transaction=" + transaction.getHash() +
                ", block=" + block +
                ", tokens=" + tokens +
                ", to='" + to + '\'' +
                '}';
    }
}
