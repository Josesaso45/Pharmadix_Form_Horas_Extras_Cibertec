import { useState, useEffect } from 'react';
import { FileUp, FileDown, ArrowDownRight, ArrowUpRight } from 'lucide-react';
import { useToast } from '@/components/ui/use-toast';
import AdminLayout from '@/components/layout/AdminLayout';

interface HojaTiempoAPI {
    id: number;
    numeroHoja: string;
    loteId: number | null;
    turno: string;
    estado: string;
    registros: {
        id: number;
        actividad: string;
        horasTotales: number | null;
        estado: string;
    }[];
}

export default function CalculoHH() {
    const { toast } = useToast();
    const [hojas, setHojas] = useState<HojaTiempoAPI[]>([]);
    const [loading, setLoading] = useState(false);

    // KPI state
    const [kpis, setKpis] = useState({
        planificadas: 320, // Mock for now
        reales: 0,
        costoTotal: 0,
        eficiencia: 0,
    });

    const costoPorHora = 15; // S/. 15 por hora (mock cost)

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        setLoading(true);
        try {
            // Intentamos traer datos del backend real en puerto 3000
            const token = localStorage.getItem('pharmadix_token') || '';
            // Si no tenemos token, hacemos un fetch general para demo (depende de cómo configures auth)
            // Ajuste para propósitos de la demo si es necesario
            const response = await fetch('http://localhost:3000/hojas?tomadorId=1', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const data = await response.json();
                setHojas(data);
                calculateKPIs(data);
            } else {
                // Fallback a un mock si falla la auth
                console.warn("No se pudieron cargar datos reales. Usando datos por defecto.");
                toast({
                    title: "Advertencia",
                    description: "No se pudieron obtener datos del backend. Asegúrese de haber iniciado sesión.",
                    variant: "destructive"
                });
            }
        } catch (error) {
            console.error('Error fetching data:', error);
        } finally {
            setLoading(false);
        }
    };

    const calculateKPIs = (data: HojaTiempoAPI[]) => {
        let reales = 0;

        data.forEach(hoja => {
            hoja.registros.forEach(registro => {
                if (registro.horasTotales) {
                    reales += registro.horasTotales;
                }
            });
        });

        const costoTotal = reales * costoPorHora;
        const eficiencia = (kpis.planificadas / (reales || 1)) * 100;

        setKpis(prev => ({
            ...prev,
            reales: Number(reales.toFixed(2)),
            costoTotal: Number(costoTotal.toFixed(2)),
            eficiencia: reales > 0 ? Number(eficiencia.toFixed(1)) : 0
        }));
    };

    return (
        <AdminLayout>
            {/* Header */}
            <header className="bg-white border-b border-slate-200 px-8 py-6 sticky top-0 z-10 flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-extrabold text-[#1a227f]">Cálculo de Horas Hombre – Costeo de Producción</h2>
                    <p className="text-slate-500 text-sm">Presupuesto de Mano de Obra por Lote</p>
                </div>
                <div className="flex gap-3">
                    <button className="flex items-center gap-2 px-4 py-2 border border-slate-200 rounded-lg hover:bg-slate-50 font-bold text-sm transition-colors text-slate-700">
                        <FileUp className="text-green-600 w-4 h-4" />
                        Exportar Excel
                    </button>
                    <button className="flex items-center gap-2 px-4 py-2 border border-slate-200 rounded-lg hover:bg-slate-50 font-bold text-sm transition-colors text-slate-700">
                        <FileDown className="text-red-600 w-4 h-4" />
                        Exportar PDF
                    </button>
                </div>
            </header>

            <div className="p-8 space-y-6 bg-slate-50 flex-1">
                {/* Filter Bar */}
                <section className="bg-white p-4 rounded-xl shadow-sm border border-slate-100">
                    <div className="grid grid-cols-1 md:grid-cols-4 lg:grid-cols-7 gap-4 items-end">
                        <div className="space-y-1.5">
                            <label className="text-xs font-bold text-slate-500 uppercase tracking-tight">Lote</label>
                            <select className="flex h-9 w-full rounded-md border border-slate-200 bg-transparent px-3 py-1 text-sm shadow-sm focus:ring-[#7B2FBE]">
                                <option>LT-2024-001</option>
                                <option>LT-2024-002</option>
                            </select>
                        </div>
                        <div className="space-y-1.5">
                            <label className="text-xs font-bold text-slate-500 uppercase tracking-tight">Proceso</label>
                            <select className="flex h-9 w-full rounded-md border border-slate-200 bg-transparent px-3 py-1 text-sm shadow-sm focus:ring-[#7B2FBE]">
                                <option>Todos</option>
                                <option>Granulación Húmeda</option>
                                <option>Mezclado</option>
                                <option>Encapsulado</option>
                            </select>
                        </div>
                        <div className="space-y-1.5">
                            <label className="text-xs font-bold text-slate-500 uppercase tracking-tight">Producto</label>
                            <select className="flex h-9 w-full rounded-md border border-slate-200 bg-transparent px-3 py-1 text-sm shadow-sm focus:ring-[#7B2FBE]">
                                <option>Amoxicilina 500mg</option>
                                <option>Paracetamol 1g</option>
                            </select>
                        </div>
                        <div className="space-y-1.5">
                            <label className="text-xs font-bold text-slate-500 uppercase tracking-tight">Área</label>
                            <select className="flex h-9 w-full rounded-md border border-slate-200 bg-transparent px-3 py-1 text-sm shadow-sm focus:ring-[#7B2FBE]">
                                <option>Manufactura</option>
                                <option>Acondicionado</option>
                            </select>
                        </div>
                        <div className="space-y-1.5">
                            <label className="text-xs font-bold text-slate-500 uppercase tracking-tight">Presentación</label>
                            <select className="flex h-9 w-full rounded-md border border-slate-200 bg-transparent px-3 py-1 text-sm shadow-sm focus:ring-[#7B2FBE]">
                                <option>Cápsulas</option>
                                <option>Tabletas</option>
                            </select>
                        </div>
                        <div className="space-y-1.5">
                            <label className="text-xs font-bold text-slate-500 uppercase tracking-tight">Fecha</label>
                            <input type="date" defaultValue="2024-05-20" className="flex h-9 w-full rounded-md border border-slate-200 bg-transparent px-3 py-1 text-sm shadow-sm focus:ring-[#7B2FBE]" />
                        </div>
                        <button onClick={fetchData} className="h-9 bg-[#1a227f] text-white rounded-md font-bold text-sm uppercase tracking-wide hover:bg-[#1a227f]/90 shadow-sm flex items-center justify-center">
                            {loading ? "Cargando..." : "Calcular"}
                        </button>
                    </div>
                </section>

                {/* KPI Cards */}
                <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-100">
                        <p className="text-slate-500 text-sm font-medium">Total HH Planificadas</p>
                        <div className="flex items-baseline gap-2 mt-2">
                            <h3 className="text-3xl font-bold text-slate-800">{kpis.planificadas}</h3>
                            <span className="text-slate-400 font-semibold text-lg">h</span>
                        </div>
                    </div>

                    <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-100">
                        <p className="text-slate-500 text-sm font-medium">Total HH Reales</p>
                        <div className="flex items-baseline gap-2 mt-2">
                            <h3 className="text-3xl font-bold text-slate-800">{kpis.reales}</h3>
                            <span className="text-slate-400 font-semibold text-lg">h</span>
                        </div>
                    </div>

                    <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-100">
                        <p className="text-slate-500 text-sm font-medium">Eficiencia</p>
                        <div className="flex items-center gap-3 mt-2">
                            <h3 className="text-3xl font-bold text-slate-800">{kpis.eficiencia}%</h3>
                            <div className="bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded text-xs font-bold flex items-center gap-1">
                                <ArrowUpRight className="w-3 h-3" />
                                +2.4%
                            </div>
                        </div>
                    </div>

                    <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-100 border-l-4 border-l-[#1a227f]">
                        <p className="text-slate-500 text-sm font-medium">Costo Total MO</p>
                        <div className="flex items-baseline gap-2 mt-2">
                            <span className="text-[#1a227f] font-bold">S/.</span>
                            <h3 className="text-3xl font-bold text-slate-800">{kpis.costoTotal.toLocaleString('es-PE', { minimumFractionDigits: 2 })}</h3>
                        </div>
                    </div>
                </section>

                {/* Table and Chart Row */}
                <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                    {/* Table Area */}
                    <div className="xl:col-span-2 bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden">
                        <div className="p-4 border-b border-slate-100 flex justify-between items-center">
                            <h4 className="font-bold text-slate-700">Desglose de Producción</h4>
                            <span className="text-xs bg-slate-100 px-2 py-1 rounded text-slate-500">Actualizado: Ahora</span>
                        </div>
                        <div className="overflow-x-auto">
                            <table className="w-full text-left text-sm border-collapse">
                                <thead>
                                    <tr className="bg-slate-50 text-slate-600 font-bold">
                                        <th className="px-4 py-3 border-b border-slate-100">Hoja/Lote</th>
                                        <th className="px-4 py-3 border-b border-slate-100">Registros</th>
                                        <th className="px-4 py-3 border-b border-slate-100 text-right">HH Plan</th>
                                        <th className="px-4 py-3 border-b border-slate-100 text-right">HH Real</th>
                                        <th className="px-4 py-3 border-b border-slate-100 text-center">Var.</th>
                                        <th className="px-4 py-3 border-b border-slate-100 text-right">Costo (S/.)</th>
                                        <th className="px-4 py-3 border-b border-slate-100 text-center">Estado</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-100">
                                    {hojas.map((hoja, i) => {
                                        // Datos calculados para la fila
                                        const hhPlan = 80 + (i * 10); // Mock
                                        const hhReal = hoja.registros.reduce((acc, r) => acc + (r.horasTotales || 0), 0);
                                        const costo = hhReal * costoPorHora;

                                        return (
                                            <tr key={hoja.id} className="hover:bg-slate-50 transition-colors">
                                                <td className="px-4 py-4 font-bold text-[#1a227f]">{hoja.numeroHoja}</td>
                                                <td className="px-4 py-4">{hoja.registros.length} actividades</td>
                                                <td className="px-4 py-4 text-right">{hhPlan}</td>
                                                <td className="px-4 py-4 text-right">{hhReal.toFixed(1)}</td>
                                                <td className="px-4 py-4 text-center">
                                                    {hhReal <= hhPlan ? (
                                                        <ArrowDownRight className="text-emerald-500 w-4 h-4 mx-auto" />
                                                    ) : (
                                                        <ArrowUpRight className="text-red-500 w-4 h-4 mx-auto" />
                                                    )}
                                                </td>
                                                <td className="px-4 py-4 text-right">{costo.toLocaleString('es-PE', { minimumFractionDigits: 2 })}</td>
                                                <td className="px-4 py-4 text-center">
                                                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-tighter ${hoja.estado === 'cerrada' ? 'bg-emerald-100 text-emerald-700' : 'bg-blue-100 text-blue-700'}`}>
                                                        {hoja.estado}
                                                    </span>
                                                </td>
                                            </tr>
                                        );
                                    })}
                                    {hojas.length === 0 && (
                                        <tr>
                                            <td colSpan={7} className="px-4 py-8 text-center text-slate-500">
                                                No hay datos para mostrar.
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    {/* Chart Area */}
                    <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-6 flex flex-col">
                        <div className="mb-6">
                            <h4 className="font-bold text-slate-700">HH Planificadas vs Reales</h4>
                            <p className="text-xs text-slate-400">Comparativo Rápido</p>
                        </div>
                        <div className="flex-1 flex flex-col justify-center gap-6 py-4">
                            {hojas.map((hoja, i) => {
                                const hhPlan = 80 + (i * 10);
                                const hhReal = hoja.registros.reduce((acc, r) => acc + (r.horasTotales || 0), 0);
                                const porcentaje = Math.min((hhReal / hhPlan) * 100, 100);

                                return (
                                    <div key={hoja.id} className="space-y-2">
                                        <div className="flex justify-between text-xs font-bold uppercase tracking-wider text-slate-500">
                                            <span>{hoja.numeroHoja}</span>
                                            <span>{hhPlan}h vs {hhReal.toFixed(1)}h</span>
                                        </div>
                                        <div className="h-6 w-full bg-slate-100 rounded-full overflow-hidden flex">
                                            <div className="h-full bg-[#1a227f]/40 relative" style={{ width: '100%' }}>
                                                <div className="h-full bg-[#7B2FBE] absolute left-0 top-0" style={{ width: `${porcentaje}%` }}></div>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                            {hojas.length === 0 && (
                                <div className="text-center text-slate-500 text-sm my-auto">
                                    Sin datos gráficos
                                </div>
                            )}
                        </div>
                        <div className="mt-6 flex gap-4 text-xs font-medium">
                            <div className="flex items-center gap-1.5">
                                <span className="w-3 h-3 bg-[#1a227f]/40 rounded-sm"></span>
                                <span>Planificado</span>
                            </div>
                            <div className="flex items-center gap-1.5">
                                <span className="w-3 h-3 bg-[#7B2FBE] rounded-sm"></span>
                                <span>Real</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </AdminLayout>
    );
}
