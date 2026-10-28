# Regras do R8. Valem só para a variante "otimizado", a única que minifica.
#
# O que está aqui existe porque o R8 não tem como adivinhar: são classes alcançadas por
# reflexão ou por nome, e nada disso aparece como erro de compilação. Quebra só em tempo
# de execução, no aparelho.

# O Firestore monta o perfil por reflexão: toObject(PerfilUsuario::class.java) lê os nomes
# dos campos e chama o construtor sem argumentos. Renomear qualquer um dos dois faz o perfil
# voltar vazio ou estourar na hora do login.
#
# As enumerações do domínio (Classe, Atributo, Titulo, Rank) são gravadas pelo nome e lidas
# de volta com valueOf, tanto no Room quanto no Firestore — os nomes das constantes também
# precisam sobreviver. O modelo inteiro é pequeno, então guardar tudo sai mais barato que
# manter uma lista que vai ficar desatualizada.
-keep class dev.pedro.pessoal.domain.model.** { *; }

# O Firestore usa a informação de tipo genérico para decidir como converter cada campo.
-keepattributes Signature
-keepattributes *Annotation*
