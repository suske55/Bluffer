package Reactor.tokenizer;

public interface TokenizerFactory<T> {
   MessageTokenizer<T> create();
}
