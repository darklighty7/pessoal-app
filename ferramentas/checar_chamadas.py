#!/usr/bin/env python3
"""
Confere chamadas de funcao e de construtor sem precisar do compilador.

Por que existe: a branch juntou seis commits sem nenhuma build no meio, e dois erros
bobos (um argumento com nome errado, um construtor com argumento a menos) so apareceram
depois de tres rodadas de `./gradlew assembleOtimizado` na maquina do Pedro. Isto nao
substitui o compilador — nao sabe tipos, nem sobrecarga, nem generics de verdade. Pega
so uma familia de erro, que e justamente a que mais aparece quando uma assinatura muda
e uma chamada fica para tras.

Uso:  python3 ferramentas/checar_chamadas.py
Roda na raiz do projeto. Sem saida de problema = nada obvio fora do lugar.
"""
import re, pathlib, collections

raiz = pathlib.Path("app/src/main/java")
arquivos = {p: p.read_text() for p in raiz.rglob("*.kt")}

def sem_comentarios(t):
    """Tira comentarios e esvazia textos entre aspas, preservando as quebras de linha.

    Esvaziar os textos importa: uma virgula dentro de "Bem-vindo, jogador" seria
    contada como separador de argumento e inventaria um erro que nao existe.
    """
    def vazio(m):
        return m.group(0)[0] + "\n" * m.group(0).count("\n") + m.group(0)[-1]
    t = re.sub(r'"""(?:.|\n)*?"""', vazio, t)
    t = re.sub(r'"(?:\\.|[^"\\\n])*"', '""', t)
    t = re.sub(r"/\*(?:.|\n)*?\*/", vazio, t)
    return re.sub(r"//[^\n]*", "", t)

def bloco(t, i):
    """Devolve o conteudo entre parenteses comecando no '(' em i."""
    nivel, j = 0, i
    while j < len(t):
        c = t[j]
        if c in "([{": nivel += 1
        elif c in ")]}":
            nivel -= 1
            if nivel == 0: return t[i+1:j], j
        j += 1
    return None, None

def partir(s):
    """Divide por virgulas de nivel zero. A seta vira simbolo para nao contar como '>'."""
    s = s.replace("->", "\u2192")
    s = re.sub(r"(?<=[\w])<(?=[\w?(])", "\u00ab", s)
    s = re.sub(r"(?<=[\w?)\]])>(?=[\s,)=\u2192]|$)", "\u00bb", s)
    partes, nivel, atual = [], 0, ""
    for c in s:
        if c in "([{\u00ab": nivel += 1
        elif c in ")]}\u00bb": nivel -= 1
        if c == "," and nivel == 0:
            partes.append(atual); atual = ""
        else:
            atual += c
    if atual.strip(): partes.append(atual)
    return [p.strip() for p in partes if p.strip()]

# --- definicoes ---
defs = collections.defaultdict(list)
for p, bruto in arquivos.items():
    t = sem_comentarios(bruto)
    for m in re.finditer(r"\bfun\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(", t):
        nome = m.group(1)
        dentro, _ = bloco(t, m.end()-1)
        if dentro is None: continue
        params = []
        for par in partir(dentro):
            pm = re.match(r"(?:(?:vararg|crossinline|noinline)\s+)?([A-Za-z_][A-Za-z0-9_]*)\s*:", par)
            if pm: params.append((pm.group(1), "=" in par.split(":",1)[1]))
        defs[nome].append((p, params))

# --- chamadas com argumento nomeado ---
problemas = []
for p, bruto in arquivos.items():
    t = sem_comentarios(bruto)
    for m in re.finditer(r"(?<![A-Za-z0-9_.])([A-Z][A-Za-z0-9_]*|[a-z][A-Za-z0-9_]*)\s*\(", t):
        nome = m.group(1)
        if nome not in defs: continue
        if re.search(r"\b(fun|class|object|interface)\s+$", t[:m.start()].rstrip()[-30:] + " "): continue
        if re.search(r"\bfun\s+" + re.escape(nome) + r"\s*$", t[:m.end()-1]): continue
        assinaturas = defs[nome]
        if len(assinaturas) != 1: continue   # sobrecarga: nao da para decidir
        _, params = assinaturas[0]
        if not params: continue
        dentro, fim = bloco(t, m.end()-1)
        if dentro is None: continue
        args = partir(dentro)
        nomeados = [a.split("=")[0].strip() for a in args
                    if re.match(r"^[A-Za-z_][A-Za-z0-9_]*\s*=(?!=)", a)]
        if not nomeados: continue
        validos = {n for n, _ in params}
        linha = t[:m.start()].count("\n") + 1
        for n in nomeados:
            if n not in validos:
                problemas.append(f"{p}:{linha}  {nome}(...)  argumento '{n}' nao existe")
        # trailing lambda conta como um argumento a mais
        resto = t[fim+1:fim+40].lstrip()
        tem_lambda = resto.startswith("{")
        if len(nomeados) == len(args) and not tem_lambda:
            faltando = [n for n, tem_default in params if not tem_default and n not in nomeados]
            if faltando:
                problemas.append(f"{p}:{linha}  {nome}(...)  falta passar: {', '.join(faltando)}")

for x in sorted(set(problemas)): print(x)
print(f"\n{len(set(problemas))} problema(s) · {len(defs)} funcoes · {len(arquivos)} arquivos")

# --- segunda passada: construtores de classe, por contagem de argumentos ---
ctors = {}
for p, bruto in arquivos.items():
    t = sem_comentarios(bruto)
    for m in re.finditer(r"\bclass\s+([A-Z][A-Za-z0-9_]*)\s*(?:@\w+\s*)?\(", t):
        dentro, _ = bloco(t, m.end()-1)
        if dentro is None: continue
        params = []
        for par in partir(dentro):
            pm = re.search(r"([A-Za-z_][A-Za-z0-9_]*)\s*:", par)
            if pm: params.append((pm.group(1), "=" in par.split(":",1)[1]))
        if m.group(1) in ctors: ctors[m.group(1)] = None
        else: ctors[m.group(1)] = params

ruins = []
for p, bruto in arquivos.items():
    t = sem_comentarios(bruto)
    for nome, params in ctors.items():
        if not params: continue
        obrig = sum(1 for _, d in params if not d)
        for m in re.finditer(r"(?<![A-Za-z0-9_.])" + re.escape(nome) + r"\s*\(", t):
            antes = t[:m.start()].rstrip()
            if re.search(r"\b(class|interface|object)\s*$", antes) or antes.endswith(":"): continue
            dentro, _ = bloco(t, m.end()-1)
            if dentro is None: continue
            args = partir(dentro)
            if any(re.match(r"^[A-Za-z_][A-Za-z0-9_]*\s*=(?!=)", a) for a in args): continue
            if not (obrig <= len(args) <= len(params)):
                linha = t[:m.start()].count("\n") + 1
                ruins.append(f"{p}:{linha}  {nome}(...) recebeu {len(args)}, esperava {obrig}..{len(params)}")

for x in sorted(set(ruins)): print(x)
print(f"{len(set(ruins))} problema(s) em construtores · {len([c for c in ctors.values() if c])} classes")
