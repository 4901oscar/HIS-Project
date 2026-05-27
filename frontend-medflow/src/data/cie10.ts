// Catálogo CIE-10 — códigos más frecuentes en consulta general
export interface Cie10Item { code: string; description: string; }

const CIE10: Cie10Item[] = [
  // Enfermedades infecciosas
  { code: 'A00', description: 'Cólera' },
  { code: 'A01.0', description: 'Fiebre tifoidea' },
  { code: 'A06.0', description: 'Disentería amebiana' },
  { code: 'A09', description: 'Diarrea y gastroenteritis de presunto origen infeccioso' },
  { code: 'A15.0', description: 'Tuberculosis pulmonar con confirmación bacteriológica' },
  { code: 'A37.0', description: 'Tos ferina por Bordetella pertussis' },
  { code: 'A90', description: 'Dengue clásico' },
  { code: 'A91', description: 'Fiebre hemorrágica debida al virus del dengue' },

  // Neoplasias
  { code: 'C18', description: 'Tumor maligno del colon' },
  { code: 'C34', description: 'Tumor maligno de bronquios y pulmón' },
  { code: 'C50', description: 'Tumor maligno de la mama' },
  { code: 'C53', description: 'Tumor maligno del cuello del útero' },
  { code: 'C61', description: 'Tumor maligno de la próstata' },
  { code: 'D50.0', description: 'Anemia por déficit de hierro secundaria a pérdida de sangre' },
  { code: 'D64', description: 'Otras anemias' },

  // Endocrinología
  { code: 'E10', description: 'Diabetes mellitus tipo 1' },
  { code: 'E11', description: 'Diabetes mellitus tipo 2' },
  { code: 'E11.0', description: 'Diabetes mellitus tipo 2 con coma' },
  { code: 'E11.5', description: 'Diabetes mellitus tipo 2 con complicaciones circulatorias periféricas' },
  { code: 'E11.9', description: 'Diabetes mellitus tipo 2 sin complicaciones' },
  { code: 'E14', description: 'Diabetes mellitus no especificada' },
  { code: 'E03.9', description: 'Hipotiroidismo no especificado' },
  { code: 'E05.9', description: 'Tirotoxicosis no especificada' },
  { code: 'E66', description: 'Obesidad' },
  { code: 'E78.0', description: 'Hipercolesterolemia pura' },
  { code: 'E78.5', description: 'Hiperlipidemia mixta' },

  // Sistema nervioso
  { code: 'G40', description: 'Epilepsia' },
  { code: 'G43', description: 'Migraña' },
  { code: 'G43.0', description: 'Migraña sin aura (migraña común)' },
  { code: 'G43.1', description: 'Migraña con aura (migraña clásica)' },
  { code: 'G44.2', description: 'Cefalea tensional' },
  { code: 'G47.0', description: 'Insomnio' },
  { code: 'G51.0', description: 'Parálisis de Bell' },
  { code: 'G62.9', description: 'Polineuropatía no especificada' },

  // Trastornos mentales
  { code: 'F10', description: 'Trastornos mentales y del comportamiento debidos al uso de alcohol' },
  { code: 'F20', description: 'Esquizofrenia' },
  { code: 'F32', description: 'Episodio depresivo' },
  { code: 'F32.0', description: 'Episodio depresivo leve' },
  { code: 'F32.1', description: 'Episodio depresivo moderado' },
  { code: 'F32.2', description: 'Episodio depresivo grave sin síntomas psicóticos' },
  { code: 'F40.0', description: 'Agorafobia' },
  { code: 'F41.0', description: 'Trastorno de pánico' },
  { code: 'F41.1', description: 'Trastorno de ansiedad generalizada' },
  { code: 'F43.1', description: 'Trastorno de estrés postraumático' },

  // Enfermedades del ojo
  { code: 'H10', description: 'Conjuntivitis' },
  { code: 'H25', description: 'Catarata senil' },
  { code: 'H40', description: 'Glaucoma' },
  { code: 'H52.1', description: 'Miopía' },
  { code: 'H52.2', description: 'Astigmatismo' },

  // Enfermedades del oído
  { code: 'H65', description: 'Otitis media no supurativa' },
  { code: 'H66', description: 'Otitis media supurativa' },
  { code: 'H81.0', description: 'Enfermedad de Menière' },

  // Enfermedades cardiovasculares
  { code: 'I10', description: 'Hipertensión esencial (primaria)' },
  { code: 'I11', description: 'Enfermedad cardíaca hipertensiva' },
  { code: 'I20', description: 'Angina de pecho' },
  { code: 'I21', description: 'Infarto agudo de miocardio' },
  { code: 'I25', description: 'Enfermedad isquémica crónica del corazón' },
  { code: 'I48', description: 'Fibrilación y aleteo auricular' },
  { code: 'I50', description: 'Insuficiencia cardíaca' },
  { code: 'I63', description: 'Infarto cerebral' },
  { code: 'I64', description: 'Accidente vascular encefálico (AVE)' },
  { code: 'I84', description: 'Hemorroides' },

  // Enfermedades respiratorias
  { code: 'J00', description: 'Rinofaringitis aguda (resfriado común)' },
  { code: 'J01', description: 'Sinusitis aguda' },
  { code: 'J02', description: 'Faringitis aguda' },
  { code: 'J03', description: 'Amigdalitis aguda' },
  { code: 'J04', description: 'Laringitis y traqueítis aguda' },
  { code: 'J06', description: 'Infección aguda de las vías respiratorias superiores (IRA alta)' },
  { code: 'J10', description: 'Influenza (gripe) debida a virus de la gripe identificado' },
  { code: 'J11', description: 'Influenza (gripe) debida a virus no identificado' },
  { code: 'J18', description: 'Neumonía no especificada' },
  { code: 'J20', description: 'Bronquitis aguda' },
  { code: 'J45', description: 'Asma' },
  { code: 'J45.0', description: 'Asma predominantemente alérgica' },
  { code: 'J45.1', description: 'Asma no alérgica' },

  // Enfermedades digestivas
  { code: 'K01', description: 'Dientes incluidos e impactados' },
  { code: 'K04', description: 'Enfermedades de la pulpa y los tejidos periapicales' },
  { code: 'K21', description: 'Enfermedad por reflujo gastroesofágico (ERGE)' },
  { code: 'K25', description: 'Úlcera gástrica' },
  { code: 'K26', description: 'Úlcera duodenal' },
  { code: 'K29', description: 'Gastritis y duodenitis' },
  { code: 'K35', description: 'Apendicitis aguda con peritonitis generalizada' },
  { code: 'K37', description: 'Apendicitis no especificada' },
  { code: 'K40', description: 'Hernia inguinal' },
  { code: 'K57', description: 'Enfermedad diverticular del intestino' },
  { code: 'K70', description: 'Enfermedad hepática alcohólica' },
  { code: 'K74', description: 'Fibrosis y cirrosis del hígado' },
  { code: 'K80', description: 'Colelitiasis (cálculos biliares)' },
  { code: 'K81', description: 'Colecistitis' },
  { code: 'K92.1', description: 'Melena' },

  // Enfermedades de la piel
  { code: 'L02', description: 'Absceso cutáneo, forúnculo y ántrax' },
  { code: 'L03', description: 'Celulitis' },
  { code: 'L20', description: 'Dermatitis atópica' },
  { code: 'L23', description: 'Dermatitis alérgica de contacto' },
  { code: 'L29', description: 'Prurito' },
  { code: 'L40', description: 'Psoriasis' },
  { code: 'L50', description: 'Urticaria' },

  // Sistema musculoesquelético
  { code: 'M06.9', description: 'Artritis reumatoide no especificada' },
  { code: 'M10', description: 'Gota' },
  { code: 'M16', description: 'Coxartrosis (artrosis de cadera)' },
  { code: 'M17', description: 'Gonartrosis (artrosis de rodilla)' },
  { code: 'M54.2', description: 'Cervicalgia' },
  { code: 'M54.4', description: 'Lumbago con ciática' },
  { code: 'M54.5', description: 'Lumbago no especificado' },
  { code: 'M75.1', description: 'Síndrome del manguito de los rotadores' },
  { code: 'M79.3', description: 'Paniculitis' },

  // Enfermedades genitourinarias
  { code: 'N10', description: 'Nefritis tubulointersticial aguda (pielonefritis aguda)' },
  { code: 'N18', description: 'Enfermedad renal crónica' },
  { code: 'N20', description: 'Cálculo del riñón y del uréter (litiasis renal)' },
  { code: 'N30', description: 'Cistitis' },
  { code: 'N39.0', description: 'Infección de vías urinarias (IVU) sin localización especificada' },
  { code: 'N40', description: 'Hiperplasia benigna de la próstata' },
  { code: 'N43', description: 'Hidrocele y espermatocele' },

  // Ginecología y obstetricia
  { code: 'N73', description: 'Otras enfermedades inflamatorias pélvicas femeninas' },
  { code: 'N76', description: 'Otras inflamaciones de la vagina y de la vulva' },
  { code: 'N91', description: 'Menstruación ausente, escasa o rara' },
  { code: 'N92', description: 'Menstruación excesiva, frecuente e irregular' },
  { code: 'O20.0', description: 'Amenaza de aborto' },
  { code: 'O80', description: 'Parto único espontáneo' },

  // Traumatismos y causas externas
  { code: 'S00', description: 'Traumatismo superficial de la cabeza' },
  { code: 'S06', description: 'Traumatismo intracraneal' },
  { code: 'S09.9', description: 'Traumatismo de la cabeza no especificado' },
  { code: 'S52', description: 'Fractura del antebrazo' },
  { code: 'S62', description: 'Fractura en la muñeca y mano' },
  { code: 'S72', description: 'Fractura del fémur' },
  { code: 'S80', description: 'Traumatismo superficial de la pierna' },
  { code: 'S82', description: 'Fractura de la pierna (incluye tobillo)' },
  { code: 'T14', description: 'Traumatismo de región del cuerpo no especificada' },
  { code: 'T39', description: 'Intoxicación por analgésicos y antipiréticos no narcóticos' },
  { code: 'T78.1', description: 'Otras reacciones alérgicas' },
  { code: 'T78.4', description: 'Alergia no especificada' },

  // Síntomas y signos generales
  { code: 'R00.0', description: 'Taquicardia no especificada' },
  { code: 'R00.1', description: 'Bradicardia no especificada' },
  { code: 'R05', description: 'Tos' },
  { code: 'R06.0', description: 'Disnea' },
  { code: 'R07.0', description: 'Dolor de garganta' },
  { code: 'R07.4', description: 'Dolor torácico no especificado' },
  { code: 'R10.0', description: 'Abdomen agudo' },
  { code: 'R10.4', description: 'Dolor abdominal no especificado' },
  { code: 'R11', description: 'Náuseas y vómitos' },
  { code: 'R19.7', description: 'Diarrea no especificada' },
  { code: 'R50.9', description: 'Fiebre no especificada' },
  { code: 'R51', description: 'Cefalea' },
  { code: 'R55', description: 'Síncope y colapso' },
  { code: 'R68.9', description: 'Síntoma o signo general no especificado' },

  // COVID-19
  { code: 'U07.1', description: 'COVID-19, virus identificado' },
  { code: 'U07.2', description: 'COVID-19, virus no identificado' },
];

export default CIE10;
